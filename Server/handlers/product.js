//<table border="1">
//	<tr>
//		<th>routes ajoutées dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Ajout des modules node.js
//<ul>
// <li>  <a href="https://github.com/chriso/node-validator"><b>Validator</b></a> : ensemble de fonction de format de String</li>
//</ul>
validator = require("validator");

//Route GET <b>/product</b><br />
// <small>route renvoyant des résultats de produit similaire au mot product ou le produit correspondant au code EAN product</small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>product</b> : nom d'un produit ou code EAN</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>page HTML</b> : products ou product</li>
//		<li><b>document JSON</b> : résultats de la recherche</li>
//	</ul>
//</ul>
exports.getProduct = function(config, queryStatIncr, pg, errorGes) {
	return (function(req, res) {
		var user = req.get("user-agent");
		var queryProduct;
		req.query.product = req.query.product.replaceBadCharacter().trim();
		if (!req.query.product || req.query.product.length === 0) { // la recherche est vide alors renvoit vers la page home
			res.redirect('/');
		} else {
			queryStatIncr("product_research", function(err, resultStatIncr) {
				/* incrémentation de la variable connection
				dans Redis pour les statistiques*/
				if (!err) {
					if (validator.isNumeric(req.query.product) && (req.query.product.length == 13 || req.query.product.length == 8)) { // si product est un numéro de taille 13 ou 8 alors la recherche se fera par code EAN dans la base postgreSQL
						queryProduct = 'SELECT x.product_name, x.product_ean, x.product_descriptive, '+ // création de la requête
						'\'gtin-\' || left(x.product_ean, 3) as product_img, y.brand_name, y.brand_sin FROM product x, brand y '+
						'WHERE x.brand_sin = y.brand_sin '+
						'AND x.product_ean = \'' + req.query.product + '\';';
						pg(queryProduct, function(err, result) { // envoit de la requête à postgreSQL
							if (err) {errorGes(user, res, 'login');}
							else if (user == 'scanoid/1.0' || req.query.embed) // renvoit des résultats selon l'user-agent
								res.json({registered : "success", results : result.rows[0]});
							else
								res.render('product', {web : config.web, co : res.get('co'), results : result.rows[0]});
						});
					} else { // recherche par nom de produit
						var number = (user == 'scanoid/1.0') ? 10 : 16; // défini le nombre de résultat à renvoyer selon l'user-agent
						queryProduct = 'SELECT x.product_name as name, x.product_ean as ean, \'gtin-\' || left(x.product_ean, 3) as img, ' +
						'y.brand_name, y.brand_sin, x.product_descriptive FROM product x, brand y WHERE x.brand_sin = y.brand_sin '+
						'AND x.product_vectorise @@ plainto_tsquery(\''+ req.query.product +'\') LIMIT ' + number + ';';
						pg(queryProduct, function(err, result) { // requête à postgreSQL
							if (err) {
								if (user == 'scanoid/1.0')
									res.json({registered : "failure", err : ['no such product']});
								else
									res.redirect('/');
							} else if (user == 'scanoid/1.0') // renvoit des résultats selon l'user-agent
							res.json({registered : "success", results : result.rows});
							else
								res.render('products', {web : config.web, co : res.get('co'), search : req.query.product ,results: result.rows});
						});
					}
				} else {
					errorGes(user, res, 'login');
				}
			});
		}
	});
};

//Route GET <b>/productname</b><br />
// <small>route renvoyant des noms de produit similaire au mot product</small><br />
// <small>route appelé dans <a href="jquery-lookingforproduct.html"><b>jquery-lookingforproduct.js</b></a></small> 
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>product</b> : nom d'un produit</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : résultats de la recherche</li>
//	</ul>
//</ul>
exports.getProductName = function(config, pg, errorGes) {
	return (function(req, res) {
		var user = req.get("user-agent");
		req.query.product = req.query.product.replaceBadCharacter().trim();
		var queryProduct = 'SELECT product_name as name FROM product WHERE '+
		' product_vectorise @@ plainto_tsquery(\''+ req.query.product +'\') LIMIT 4;';
		pg(queryProduct, function(err, result) { // envoit de la requête à postgreSQL
			if (err)
				res.json({registered : "failure", err : ['no such product']});
			else
				res.json({registered : "success", results : result.rows});
		});
	});	
};


//Route GET <b>/productnext</b><br />
// <small>route renvoyant les résultats suivants dans postgreSQL de produit similaire au mot product</small><br />
// <small>route appelé dans <a href="jquery-product.html"><b>jquery-product.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>product</b> : nom d'un produit ou code EAN</li>
//		<li><b>number</b> : index des résultats envoyés précédemment</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>page HTML</b> : products ou product</li>
//		<li><b>document JSON</b> : résultats de la recherche</li>
//	</ul>
//</ul>
exports.getProductNext = function(config, pg, errorGes) {
	return (function (req, res) {
		req.query.product = req.query.product.replaceBadCharacter().trim();
		var user = req.get("user-agent");
		if (req.query.number && req.query.number) {
			var queryProduct = 'SELECT x.product_name as name, x.product_ean as ean, \'gtin-\' || left(x.product_ean, 3) as img, '+
			'y.brand_name, y.brand_sin FROM product x, brand y WHERE x.brand_sin = y.brand_sin '+
			'AND x.product_vectorise @@ plainto_tsquery(\''+ req.query.product +'\') LIMIT 16 OFFSET '+ req.query.number +';';
			pg(queryProduct, function(err, result) { // envoit de la requête à postgreSQL
				if (result.rows.length === 0) {
					res.render('productNext', {results: result.rows});
				} else if (err) {
					if (user == 'scanoid/1.0')
						res.json({registered : "failure", err : ['no such product']});
					else
						res.redirect('/');
				} else if (user == 'scanoid/1.0') {  // renvoit des résultats selon l'user-agent
					res.json({registered : "success", results : result.rows});
				} else {
					res.render('productNext', {results: result.rows});
				}
			});
		}
	});
};

//Route POST <b>/product/:product_ean/shop/:shop_id</b><br />
// <small>route pour entrer un nouveau prix pour un produit dans un magasin</small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>product_ean</b> : code EAN du produit</li>
//		<li><b>shop_id</b> : identifiant du magasin dans la base de données postgreSQL</li>
//		<li><b>prix</b> : nouveau prix</li>
//		<li><b>promotion</b> : booléen vrai si le nouveau prix est une promotion</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.postProductPrice = function(config, redisfunc, queryStatIncr, pg, errorGes) {
	return (function(req, res) {
		var user = req.get('user-agent');
		var element = {
			email : res.get('user'),
			id : req.params.shop_id.replaceBadCharacter(),
			ean : req.params.product_ean.replaceBadCharacter(),
			price : req.params.price,
			promotion : req.params.promotion
		};
		redisfunc(element, function (err, result) {
			if (!err) {
				queryStatIncr("price_entry", function(err, resultStatIncr) {
					if (!err) {
						res.json({registered : "success"});
					} else {
						res.json({registered : "failure", err : [err]});
					}
				});
			}
			else {
				res.json({registered : "failure", err : [err]});
			}
		});
	});
};