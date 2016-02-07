//<table border="1">
//	<tr>
//		<th>route ajoutée dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Route GET <b>/(home)?</b><br />
// <small>route renvoyant la page d'acceuil</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>Page HTML</b> : avec les informations pour le navigateur</li>
//	</ul>
//</ul>
exports.getHome = function(config) {
	return (function(req, res){
		res.render('home',  {web : config.web, co : res.get("co")});
	});
};