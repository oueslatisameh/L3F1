//<table border="1">
//	<tr>
//		<th>routes ajoutées dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Ajout des modules node.js
//<ul>
// <li>  <a href="https://github.com/chriso/node-validator"><b>Validator</b></a> : ensemble de fonction de format de String</li>
//</ul>
var validator = require('validator');
	
//Route GET <b>/card</b><br />
// <small>route renvoyant les cartes de l'utilisateur</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> contenant les informations pour l'application</li>
//		<li><b>Page HTML</b> : avec les informations pour le navigateur</li>
//	</ul>
//</ul>
exports.getCard = function(config, pg, errorGes) {
	return (function(req, res) {
		var user = req.get("user-agent");
		var queryCard = 'SELECT x.card_code as code, y.brand_name as name, y.brand_sin as bsin, ' + // creéation de la requête
			'length(x.card_code) as length FROM loyalty_card x, brand y WHERE x.user_email = \'' + 
			res.get('user') + '\' AND x.brand_sin = y.brand_sin;';
		pg(queryCard, function(err, result) { // envoit de la requête 
			if (err) {errorGes(user, res, 'login');}
			else if (user == 'scanoid/1.0') { // renvoit d'une réponse selon le client
				res.json({registered : "success", results : result.rows});
			} else {
				res.render('card', {web : config.web, co : true, results : result.rows});
			}
		});
	});
};

//Route POST <b>/card/:brand-:code</b><br />
// <small>route permettant la création d'une nouvelle carte</small><br />
// <small>route appelé dans <a href="jquery-card.html"><b>jquery-card.js</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>brand</b> : nom de la marque de la carte</li>
//		<li><b>code</b> : code EAN de la carte</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.postCard = function(config, pg, errorGes) {
	return (function(req, res) {
		req.params.brand = req.params.brand.replaceBadCharacter().trim();
		if (!validator.isNumeric(req.params.code)) // vérification du champ code
			res.json({registered : "failure", err : ["doit contenir que des chiffres"]});
		else {
			var queryCard = 'INSERT INTO loyalty_card (card_code, brand_sin, user_email) select \'' + // création de la requête
				req.params.code + '\', brand_sin, \'' + res.get('user') + 
				'\' from brand where brand_name = \'' + req.params.brand + '\';';
			pg(queryCard, function(err, result) { // envoit de la requête à postgreSQL
				if (err)
					res.json({registered : "failure", err : [config.stringError.serverError]});
				else
					res.json({registered : "success"}); // confirmation de la création de la carte
			});
		}
	});
};

//Route PUT <b>/card/:brand-:code</b><br />
// <small>route permettant la modification du code d'une carte</small><br />
// <small>route appelé dans <a href="jquery-card.html"><b>jquery-card.js</b></a></small> 
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>brand</b> : nom de la marque de la carte</li>
//		<li><b>code</b> : code EAN de la carte</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.putCard = function(config, pg, errorGes) {
	return (function(req, res) {
		req.params.brand = req.params.brand.replaceBadCharacter().trim();
		var user = req.get('user-agent');
		var queryCard = 'UPDATE loyalty_card SET card_code = \'' + // création de la requête
			req.params.code + '\' where user_email = \''+
			res.get('user') + '\' AND brand_sin = \'' + req.params.brand + '\';';
		pg(queryCard, function(err, result) { // envoit de la requête à postgreSQL
			if (err) {res.json({registered : "failure", err : [config.stringError.serverError]});}
			else {
				res.json({registered : "success"}); // confirmation du changement
			}
		});
	});
};

//Route DELETE <b>/card/:brand</b><br />
// <small>route supprimant une carte selon le nom</small><br />
// <small>route appelé dans <a href="jquery-card.html"><b>jquery-card.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>brand</b> : nom de la marque de la carte</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.delCard = function(config, pg, errorGes) {
	return (function(req, res) {
		req.params.brand = req.params.brand.replaceBadCharacter().trim();
		var queryCard = 'DELETE FROM loyalty_card WHERE user_email = \'' + // création de la requête
			res.get('user') + '\' AND'  +
			' brand_sin IN (SELECT brand_sin from brand where brand_name = \'' + req.params.brand + '\');';
		pg(queryCard, function(err, result) { // envoit de la requête à postgreSQL
			if (err) {
				res.json({registered : "failure"});
			} else {
				res.json({registered : "success"}); // renvoit de la confirmation
			}
		});
	});
};

//Route GET <b>/brand</b><br />
// <small>route renvoyant une liste de marque ayant un nom similaire au String brand</small><br />
// <small>route appelé dans <a href="jquery-card.html"><b>jquery-card.js</b></a></small> 
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>brand</b> : nom de la marque de la carte</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> contenant les informations pour l'application</li>
//	</ul>
//</ul>
exports.getBrand = function(config, pg, errorGes) {
	return (function(req, res) {
		req.query.brand = req.query.brand.replaceBadCharacter().trim();
		var queryBrand = 'SELECT brand_name FROM brand WHERE ' + // création de la requête
			'brand_vectorise @@ plainto_tsquery(\''+ req.query.brand +'\') ORDER BY length(brand_name) ASC LIMIT 5;';
		pg(queryBrand, function(err, result) { // envoit de la requête à postgreSQL
			if (err) {
				res.json({registered : "failure", err : [config.stringError.serverError]});
			} else {
				res.json({registered : "success", results : result.rows});						
			}
		}); 
	});
};