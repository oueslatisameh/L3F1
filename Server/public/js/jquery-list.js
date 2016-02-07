//<table border="1">
//	<tr>
//		<th>contenu dans la page </th> <th><b>list.jade</b></th>
//	</tr>
//</table>
var varNameListChange;
var varNameListDelete;

//<b>supProduct</b> : fais une requête AJAX pour supprimer un produit d'une liste de l'utilisateur<br />
// <small>fais appel à la route DELETE <a href="list.html"><b>/list/:name/product/:product</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la liste</li>
//		<li><b>ean</b> : code EAN du produit</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : suppression de l'élément dans la liste de courses</li>
//	</ul>
//</ul>
function supProduct(name, ean) {
	$.ajax({
		url : '/list/' + name.replace(/ /g, '_') + '/product/' + ean,
		type : 'DELETE',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText);
			if (result == 'success' && json.registered == 'success') {
				$("div#product" + name + "-" + ean).remove();
			}
		}
	});
}

//<b>supList</b> : fais une requête AJAX pour supprimer une liste de l'utilisateur<br />
// <small>fais appel à la route DELETE <a href="list.html"><b>/list/:name</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la liste</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : suppression de la liste de courses + du lien dans la page</li>
//	</ul>
//</ul>
function supList() {
	$.ajax({
		url : '/list/' + varNameListDelete,
		type : 'DELETE', // type de de requête HTTP
		dataType : "json", // type de données attendu
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText); // parsage de la réponse
			if (result == 'success' && json.registered == 'success') {
				$('#ModalDel').modal('hide');
				setTimeout(function() { // suppression apres que la fenêtre modal soit caché
					$('#container' + varNameListDelete).remove();
					$('#lipour' + varNameListDelete).remove();
				}, 1000);
			}
		}
	});
}

function nameListDelete(name) {
	varNameListDelete = name;
	$('#ModalDel').modal('show');
}

//<b>changeList</b> : fais une requête AJAX pour changer le nom d'une liste<br />
// <small>fais appel à la route PUT <a href="list.html"><b>/list/:old-:ne</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>old</b> : nom de la liste</li>
//		<li><b>ne</b> : nouveau nom de la liste</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : rechargement de la page</li>
//	</ul>
//</ul>
function changeList() {
	var ne = $('#ModalInputChange').val();
	$.ajax({
		url : '/list/' + varNameListChange + '-' + ne.replace(/ /g, '_'),
		type : 'PUT',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText);
			if (result == 'success' && json.registered == 'success') {
				document.location.reload(true);
			}
		}
	});
}

function nameListChange(name) {
	varNameListChange = name;
	$('#ModalChange').modal('show');
}

//<b>refresh</b> : suppression des champs dans la fenêtre modale<br />
function refresh() {
	$("#ModalSpanPut").html("");
}

//<b>addList</b> : fais une requête AJAX pour ajouter une nouvelle liste de course<br />
// <small>fais appel à la route POST <a href="list.html"><b>/list/:name</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la nouvelle liste</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : rechargement de la page</li>
//		<li><b>success</b> : affichage d'un message d'erreur dans la fenêtre modale</li>
//	</ul>
//</ul>
function addList() {
	var name = $("#ModalInputPut").val();
	$.ajax({
		url : '/list/' + name.replace(/ /g, '_'),
		type : 'POST',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText);
			if (result == 'success' && json.registered == 'success') {
				document.location.reload(true);
			} else {
				$("#ModalSpanPut").html("erreur dans le nom");
				$("#ModalInputPut").val("");
			}
		}
	});
}

//<b>saveamount</b> : fais une requête AJAX pour changer la quantité d'un produit dans une liste de courses<br />
// <small>fais appel à la route POST <a href="list.html"><b>/list/:name/product/:product/amount/:amount</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la nouvelle liste</li>
//		<li><b>ean</b> : code EAN du produit</li>
//		<li><b>amount</b> : quantité</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : affichage dans l'élément du nouveau quantité</li>
//	</ul>
//</ul>
function saveamount(name, ean) {
	var input = $("input#amount" + name + ean);
	if (input < 1 || input > 50) { // si la quantité entrée par l'utilisateur est mauvaise changement pour 1
		input.val(1);
	}
	$.ajax({
		url : '/list/' + name + '/product/' + ean + '/amount/' + input.val(), // url du serveur
		type : 'POST',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText);
			if (!(result == 'success' && json.registered == 'success')) {
				input.val(1); // changement de la quantité dans l'élément de la liste de courses
			}
		}
	});
}
