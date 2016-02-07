//<table border="1">
//	<tr>
//		<th>contenu dans la page </th> <th><b>favoriteaddress.jade</b></th>
//	</tr>
//</table>
var longitude;
var latitude;
var marker = null;
var map;

//<b>initmap</b> : initialise la carte leaflet en faisant appel au service Nominatim<br />
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>position</b> : position actuelle de l'utilisateur</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : création de la carte</li>
//	</ul>
//</ul>
function initmap(position) {
	longitude = position.coords.longitude;
	latitude = position.coords.latitude;
	map = L.map('map').setView([latitude, longitude], 16); // création de la carte
	L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { // réception des tuiles
		attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://cloudmade.com">CloudMade</a>',
		maxZoom: 18 // zoom de la carte au début
	}).addTo(map); // ajout des tuiles à la carte
}

//<b>affichMarker</b> : affiche sur la carte le marqueur à la position d'une addresse favorite et déplace
// le centre de la carte à cette endroit<br />
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>lat</b> : lat</li>
//		<li><b>lon</b> : longitude</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : création du marqueur et changement de la position de la carte</li>
//	</ul>
//</ul>
function affichMarker(lon, lat) {
	if (marker === null) {
		marker = L.marker([lat, lon]); // création du marker
		marker.addTo(map); // ajout à la carte
		map.panTo(new L.LatLng(lat, lon)); // translation du centre de la carte
	} else {
		map.removeLayer(marker); // suppression du dernier marker ajouté
		marker = L.marker([lat, lon]);
		marker.addTo(map);
		map.panTo(new L.LatLng(lat, lon));
	}
}

//<b>supaddr</b> : fais une requête AJAX pour supprimer une adresse favorite de l'utilisateur<br />
// <small>fais appel à la route DELETE <a href="Faddress.html"><b>/favorite/address:road-:town-:postcode</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>address</b> : adresse</li>
//		<li><b>town</b> : ville</li>
//		<li><b>postcode</b> : code postal</li>
//		<li><b>id</b> : identifiant de l'élément dans la liste</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : suppression de l'élément dans la liste</li>
//	</ul>
//</ul>
function supaddr(address, town, postcode, id) {
	address = address.replace(/ /g, '_');	// formatage des champs 
	town = town.replace(/ /g, '_');			// pour l'envoi de la 
	postcode = postcode.replace(/ /g, '_');	// requête AJAX
	$.ajax({
		url : '/favorite/address/' + address.replace(/\-/g, "+") + "-" + town + "-" + postcode,
		type : 'DELETE', // type de requête HTTP
		dataType : "json", // type de données attendu
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText);
			if (result == 'success' && json.registered == 'success') {
				$("div#addr" + id).remove();
			}
		}
	});
}

//<b>addaddr</b> : fais une requête AJAX pour ajouter une adresse favorite<br />
// <small>fais appel à la route POST <a href="Faddress.html"><b>/favorite/address</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>address</b> : adresse</li>
//		<li><b>town</b> : ville</li>
//		<li><b>postcode</b> : code postal</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : rechargement de la page</li>
//	</ul>
//</ul>
function addaddr() {
	var address = $("input[name='address']");	// récupération des 
	var town = $("input[name='town']");			// champs entrés par 
	var postcode = $("input[name='postcode']");	// l'utilisateur
	if (address.val() === '' || town.val() === '' || postcode.val() === ''
		|| !validator.isAlpha(town.val()) || !validator.isNumeric(postcode.val())) {
		$("div#jumb").prepend(
			$("<div class=\"alert alert-danger alert-dismissable\"></div>")
			.html("les informations entrés sont erronées ou manquantes")
				.prepend(
					$("<button type=\"button\" data-dismiss=\"alert\" aria-hidden=\"true\" class=\"close\"></button>")
						.html("&times;")
				)
		);
		return;
	}
	$.ajax({
		url : '/favorite/address/',
		type : 'POST',
		dataType : "json",
		data : {
			address : address.val().replace(/ /g, '_'),
			town : town.val().replace(/ /g, '_'),
			postcode : postcode.val().replace(/ /g, '_')
		},
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText); // parsage de la réponse
			if (result == 'success' && json.registered == 'success') {
				document.location.reload(true); // rechargement de la page
			}
		}
	});
}
