//<table border="1">
//	<tr>
//		<th>contenu dans la page </th> <th><b>card.jade</b></th>
//	</tr>
//</table>

//<b>supCard</b> : fais une requête AJAX pour supprimer une carte de l'utilisateur<br />
// <small>fais appel à la route DELETE <a href="card.html"><b>/card/:brand</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la marque</li>
//		<li><b>i</b> : indice de la carte</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : rechargement de la page</li>
//		<li><b>failure</b> : indication à l'utilisateur que la carte n'existe pas</li>
//	</ul>
//</ul>
function supCard(name, i) {
	$.ajax({
		url: '/card/' + name.replace(/ /g, '_'), // url du serveur
		type: 'DELETE', // type de requête HTTP
		dataType: "json", // type de data attendu
		complete: function(xhr, result) {
			json = JSON.parse(xhr.responseText); // parse de la réponse
			if (result == 'success' && json.registered == 'success') {
				document.location.reload(true); // rechargement de la page					
			} else {
				$("div#BodyDel" + i).html("la carte n'existe pas");
			}
		}
	});
}

//<b>refreshCard</b> : supprime le message d'erreur lancé dans supCard
function refreshCard(type, i) {
	if (type == "del")
		$("div#BodyDel"+ i).html("");
	else {
		$("input#createCardCode").val("");
	}
}

//<b>searchBrand</b> : fais une requête AJAX pour récupérer les noms de marques et les affiches dans une liste<br />
// <small>fais appel à la route GET <a href="card.html"><b>/brand</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : mot tapé par l'utilisateur</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : remplissage de la liste avec les résultats + onClick pour changer l'input createCard</li>
//	</ul>
//</ul>
function searchBrand() {
	$(document).ready(function () {
		$('#createCardName').keyup(function() { // lorsque l'utilisateur commence à entrer le nom d'un marque
			$fields = $(this);					// une requête AJAX est envoyé au serveur
			$('#listCreateCard').html('');
			if($fields.val().length > 1) {
				$.ajax({
					type : 'GET', // type de requête HTTP
					url : '/brand/',
					data : 'brand=' +$(this).val().replace(/ /g, '_'), // données envoyées 
					complete : function(xhr, result) {
						var json = JSON.parse(xhr.responseText); // parsage de la réponse
						if (result == 'success' && json.registered == 'success' && json.results) {
							for (i = 0; i < json.results.length; ++i) {
								var node = $("<a href=\"#\" class=\"list-group-item\">" + json.results[i].brand_name + "</a>");
								node.click((function(a) { // onClick sur chacun des éléments de la liste
										return (function() {
											$('#createCardName').val(a);
										});
								})(json.results[i].brand_name));
								$('#listCreateCard').append(node);
							}							
						}
					}
				});
			}
		});
	});
}

//<b>changeCard</b> : fais une requête AJAX pour changer le code d'une carte dans la base de données<br />
// <small>fais appel à la route PUT <a href="card.html"><b>/card/:brand-:code</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la marque</li>
//		<li><b>i</b> : indice de la carte</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : rechargement de la page</li>
//	</ul>
//</ul>
function changeCard(name, i) {
	var code = $("input#Code" + i).val();
	$.ajax({
		url: '/card/' + name.replace(/ /g, '_') + "-" + code,
		type: 'PUT',
		dataType: "json", 
		complete: function(xhr, result) {
			json = JSON.parse(xhr.responseText).registered;
			if (result == 'success' && json == 'success') {
				document.location.reload(true);
			}
		}
	});
}

//<b>createCard</b> : fais une requête AJAX pour créer une nouvelle carte<br />
// <small>fais appel à la route POST <a href="card.html"><b>/card/:brand-:code</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la marque</li>
//		<li><b>code</b> : code EAN de la nouvelle carte</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : rechargement de la page</li>
//		<li><b>failure</b> : mise en place d'un message d'erreur</li>
//	</ul>
//</ul>
function createCard() {
	var name = $("input#createCardName").val();
	var code = $("input#createCardCode").val();
	if (code.length != 8 && code.length != 13) {
		refreshCard("create");
		$("span#createCardSpan").html("erreur : numéro de la carte");
	} else {
		$.ajax({
			url: '/card/' + name.replace(/ /g, '_') + "-" + code,
			type: 'POST',
			dataType: "json", 
			complete: function(xhr, result) {
				json = JSON.parse(xhr.responseText).registered;
				if (result == 'success' && json == 'success') {
					document.location.reload(true);
				} else {
					$("span#createCardSpan").html("erreur dans la carte");
					$("input#createCardName").val("");
					$("input#createCardCode").val("");	
				}
			}
		});
	}
}