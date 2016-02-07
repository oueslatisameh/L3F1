//<table border="1">
//	<tr>
//		<th>contenu dans la page </th> <th><b>product.jade</b></th>
//	</tr>
//</table>
var map;
var longitude;
var latitude;
var ean;
var typeRequest;
var markersGroup = L.layerGroup([]);

function readyPosition(position) {
	longitude = position.coords.longitude;
	// position de
	latitude = position.coords.latitude;
	// l'utilisateur
	typeRequest = {
		near : {
			url : '/shop/near/product',
			data : 'ean=' + ean + "&lon=" + longitude + "&lat=" + latitude,
		},
		faddress : {
			url : '/shop/near/productFavAddr',
			data : 'ean=' + ean,
		},
		fshop : {
			url : '/shop/near/productFavShop',
			data : 'ean=' + ean,
		}
	};
	initmap();
	getAddrShop('near');
}

//<b>initmap</b> : initialise la carte leaflet en faisant appel au service Nominatim<br />
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : création de la carte + marqueurs des magasins</li>
//	</ul>
//</ul>
function initmap() {// identique à initmap dans jquery-faddress.js en dehors de l'ajout des marqueurs
	map = L.map('map').setView([latitude, longitude], 16);
	L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
		attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapnik.org/">MapNik</a>',
		maxZoom : 18
	}).addTo(map);
}

function affichMarker(lon, lat) {
	map.panTo(new L.LatLng(lat, lon));
}

function addaddr(shop_id) {
	$.ajax({
		url : '/favorite/shop/' + shop_id,
		type : 'POST',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText); // parsage de la réponse
			if (result == 'success' && json.registered == 'success') {
			}
		}
	});
}

function supaddr(shop_id) {
	$.ajax({
		url : '/favorite/shop/' + shop_id,
		type : 'DELETE',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText); // parsage de la réponse
			if (result == 'success' && json.registered == 'success') {
				document.location.reload(true);
			}
		}
	});
}

function formatageBody(nom, distance, price, promotion, type, descriptif, numero, rue, ville, postal) {
	var head = '<h4 class="list-group-item-heading"><blob>' + nom + '</blob></h4>';
	var all = '<b>prix</b> : ' + price + '&#x20AC<br />' +
	'<b>distance</b> : ' + Math.round(distance * 1000000) / 10 + 'm<br />' +
	'<b>type</b> : ' + type + '<br />' + 
	'<b>description</b> : ' + descriptif + '<br />' + 
	'<b>adresse</b> : ' + (numero || "") + ' ' + rue + ' ' + ville + ' ' + postal + '<br />';
	return (head + all);
}

function formatageButton(longitude, latitude, shop_id, name) {
	var affich = '<span onCLick=\"affichMarker(' + longitude + ', ' + latitude + ');\" ' + 'class=\"btn btn-warning btn-sm"><span class=\"glyphicon glyphicon-search\"></span></span>';
	var add = '<span onCLick=\"addaddr(' + shop_id + ');\" ' + 'class=\"btn btn-success btn-sm"><span class=\"glyphicon glyphicon-plus\"></span></span>';
	var del = null;
	if (name == "fshop") {
		del = '<span onCLick=\"supaddr(' + shop_id + ');\" ' + 'class=\"btn btn-danger btn-sm"><span class=\"glyphicon glyphicon-remove\"></span></span>';
	}
	return (affich + add + (del || ""));
}

function getAddrShop(name) {
	element = typeRequest[name];
	$.ajax({// requête AJAX
		url : element.url, // URL vers le serveur
		type : 'GET', // type de requête HTTP
		dataType : "json", // type de données attendu
		data : element.data, // envoie des coordonnées de l'utilisateur
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText);
			if (result == 'success' && json.registered == 'success') {
				var re, divElement, divText, divBody, divButton;
				var div = $('div#Shop');
				div.html("");
				markersGroup.clearLayers();
				for ( y = 0; y < json.results.length; ++y) {
					re = json.results[y];
					divElement = $('<div></div>').addClass('list-group-item');
					divText = $('<div></div>').addClass('list-group-item-text');
					divBody = $('<div></div>');
					divBody.html(formatageBody(re.shop_name, re.distance, re.price, re.ispromotion, 
						re.shop_type, re.shop_descriptive, re.shop_house_num, re.shop_road, 
						re.shop_town, re.shop_postcode));
					divButton = $('<div></div>');
					divButton.html(formatageButton(re.shop_longitude, re.shop_latitude, re.shop_id, name));
					div.append(divElement.append(divText.append(divBody).append(divButton)));
					markersGroup.addLayer(L.marker([re.shop_latitude, re.shop_longitude]).bindPopup(re.shop_name));
				}
				markersGroup.addTo(map);
				$('div#Shop').append(div);
			}
		}
	});
}