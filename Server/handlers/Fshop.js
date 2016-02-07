//<table border="1">
//	<tr>
//		<th>routes ajoutées dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Route GET <b>/favorite/shop</b><br />
// <small>route renvoyant les magasins favoris de l'utilisateur</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> contenant les informations pour l'application</li>
//		<li><b>Page HTML</b> : avec les informations pour le navigateur</li>
//	</ul>
//</ul>
exports.getFshop = function getFshop(config, pg, err) {
	return (function (req, res) {
		var user = req.get('user-agent');
		var queryFshop = 'SELECT s.* FROM favorite_shop f, shop s' + 
			' WHERE f.user_email = \'' + res.get('user') + '\' AND f.shop_id = s.shop_id;';
		pg(queryFshop, function(err, result) {
			if (err) {res.json({registered : "failure", err : [config.stringError.serverError]});}
			else {
				res.json({registered : "success", results : result.rows});
			}
		});
	});
};

//Route DELETE <b>/favorite/shop:shop_id</b><br />
// <small>route supprimant un magasin favori</small><br />
// <small>route appelé dans <a href="jquery-map.html"><b>jquery-map.js</b></a></small> 
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>shop_id</b> : numéro unique d'un magasin</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.delFshop = function delFshop(config, pg, err) {
	return (function (req, res) {
		var user = req.get('user-agent');
		req.params.shop_id = req.params.shop_id.replaceBadCharacter();
		var queryFshop = 'DELETE FROM favorite_shop where user_email = \'' + res.get('user') +
		'\' AND shop_id = \'' + req.params.shop_id + '\';';
		pg(queryFshop, function(err, result) {
			if (err) {res.json({registered : "failure", err : [config.stringError.serverError]});}
			else {
				res.json({registered : "success"});
			}
		});
	});
};

//Route POST <b>/favorite/shop</b><br />
// <small>route permettant la création d'un nouveau magasin favori</small><br />
// <small>route appelé dans <a href="jquery-map.html"><b>jquery-map.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>shop_id</b> : numéro unique d'un magasin</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.postFshop = function postFshop(config, pg, err) {
	return (function (req, res) {
		var user = req.get('user-agent');
		req.params.shop_id = req.params.shop_id.replaceBadCharacter();
		var queryFshop = 'INSERT INTO favorite_shop VALUES (' +
			'\'' + res.get('user') + '\',\'' + req.params.shop_id + '\');';
		pg(queryFshop, function(err, result) {
			if (err) {res.json({registered : "failure", err : [config.stringError.serverError]});}
			else {
				res.json({registered : "success"});
			}
		});
	});
};