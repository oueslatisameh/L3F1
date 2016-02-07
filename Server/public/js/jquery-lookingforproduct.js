//<table border="1">
//	<tr>
//		<th>contenu dans la page </th> <th><b>toutes</b></th>
//	</tr>
//</table>

// fais une requête AJAX pour chercher les produits portant un nom similaire à ce qu'a rentré l'utilisateur<br />
// <small>fais appel à la route GET <a href="card.html"><b>/productname</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : mot entré par l'utilisateur</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : rechargement de la page</li>
//		<li><b>failure</b> : indication à l'utilisateur que la carte n'existe pas</li>
//	</ul>
//</ul>
$(document).ready(function() {
	$('#lookingforproduct').keyup(function() {	// lorsque l'utilisateur commence
		$fields = $(this);						// à taper dans l'input de la 
		$('#lforproduct').html('');				// barre de navigation
		if ($fields.val().length > 1) {
			$.ajax({
				type : 'GET',
				url : '/productname/',
				data : 'product=' + $(this).val().replace(/ /g, '_'),
				complete : function(xhr, result) {
					var json = JSON.parse(xhr.responseText);
					if (result == 'success' && json.registered == 'success' && json.results) {
						for (i = 0; i < json.results.length; ++i) {
							var node = $("<option value=\"" + json.results[i].name + "\">"); // ajout des résultats
							$('#lforproduct').append(node);									 // dans la datalist
						}
					}
				}
			});
		}
	});
});
