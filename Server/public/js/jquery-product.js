//<table border="1">
//	<tr>
//		<th>contenu dans la page </th> <th><b>products.jade</b></th>
//	</tr>
//</table>
var numberall = 16;
var lists = [];

//<b>postProduct</b> : fais une requête AJAX pour ajouter un produit dans une lsite de courses<br />
// <small>fais appel à la route POST <a href="list.html"><b>/list/:name/product/:product</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la marque</li>
//		<li><b>ean</b> : code EAN du produit</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : fermeture de la fenêtre modale</li>
//	</ul>
//</ul>
function postproduct(name, ean) {
	$.ajax({
		url : '/list/' + name.replace(/ /g, '_') + '/product/' + ean,
		type : 'POST',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText).registered; // parsage de la réponse
			if (result == 'success' && json == 'success') {
				$("button[name='" + ean + "']").popover('hide');
			}
		}
	});
}

// Ajout d'un popover à chaque élément de la liste
function popoverfunc() {
	$('.lol').each(function(index) {
		var ident = $(this).attr('name');
		$(this).popover({
			title : 'Ajouter dans : ',
			trigger : "click",
			html : true,
			placement : 'right',
			content : function() {
				var test = $('<div></div>').addClass('list-group');
				for (i = 0; i < lists.length; ++i) {
					test.append($('<a></a>').addClass('list-group-item')
						.attr('onClick', 'postproduct(\''+ lists[i].name + '\', \'' + ident + '\')')
						.attr('href', '#').html(lists[i].name));
				}
				return (test.html());
			}
		});
	});
}
//<b>fulists</b> : fais une requête AJAX pour récupérer les noms des listes de courses<br />
// <small>fais appel à la route GET <a href="list.html"><b>/listname</b></a></small>
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : ajout des noms dans un tableau</li>
//	</ul>
//</ul>
function fulists() {
	$.ajax({
		url : '/listname/',
		type : 'GET',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText);
			if (result == 'success' && json.registered == 'success') {
				lists = json.results;
			}
		}
	});
}

//<b>scrolldown</b> : fais une requête AJAX pour récupérer les prochains résultats de la recherche<br />
// <small>fais appel à la route GET <a href="product.html"><b>/productnext</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>product</b> : mot recherché</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : ajout des nouveaux produits dans la liste</li>
//	</ul>
//</ul>
function scrolldown() {
	$(window).scroll(function() {
		if ($(window).scrollTop() + $(window).height() == $(document).height()) {
			$.ajax({
				url : '/productnext/',
				type : 'GET',
				async : false, // requête de façon synchrone
				data : "product=" + $('h1').attr('id').replace(/ /g, '_') + "&number=" + numberall, // données envoyées
				complete : function(xhr, result) {
					if (result == 'success') {
						numberall += 16; // indice des produits de la liste
						$('div#containerall').append(xhr.responseText); // ajout automatique du code HTML envoyé du serveur
						popoverfunc();

					}
				}
			});
		}
	});
}