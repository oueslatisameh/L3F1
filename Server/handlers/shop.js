//<table border="1">
//	<tr>
//		<th>routes ajoutées dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Route GET <b>/shop/near</b><br />
// <small>route permettant de retrouver les magasins autour de la position de l'utilisateur</small><br />
// <small>route appelé dans <a href="jquery-map.html"><b>jquery-map.js</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>lon</b> : longitude de l'utilisateur</li>
//		<li><b>lat</b> : latitude de l'utilisateur</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : résultats de la requête à postgreSQL</li>
//	</ul>
//</ul>
exports.getNearShop = function(config, pg, errorGes) {
	return (function (req, res) {
		var user = req.get("user-agent");
		var number = (user == 'scanoid/1.0') ? 1 : 5;
		var queryNearShop = 'SELECT ST_DISTANCE(shop_geom, ST_MakePoint(' + req.query.lon + ',' + req.query.lat +')) as distance, '+
			'shop_name as name , shop_descriptive as descriptive, shop_type as type, shop_house_num as numero, shop_road as rue, shop_postcode as code_postal, '+
			'shop_id as id, shop_town as ville, shop_longitude as longitude, shop_latitude as latitude '+
			'FROM shop ORDER BY distance ASC LIMIT ' + number + ' ;';
		pg(queryNearShop, function(err, result) {
			if (err) {
				res.json({registered : 'failure', err : [config.stringError.serverError]});
			} else {
				res.json({registered : 'success', results : result.rows});
			}
		});
	});
};

//Route GET <b>/shop/near/name</b><br />
// <small>route permettant de retrouver les magasins autour de la position de l'utilisateur par nom</small><br />
// <small>route appelé dans <a href="jquery-map.html"><b>jquery-map.js</b></a></small> 
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>lon</b> : longitude de l'utilisateur</li>
//		<li><b>lat</b> : latitude de l'utilisateur</li>
//		<li><b>name</b> : nom du magasin recherché</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : résultats de la requête à postgreSQL</li>
//	</ul>
//</ul>
exports.getNearShopByName = function(config, pg, errorGes) {
	return (function (req, res) {
		var user = req.get("user-agent");
		var number = (user == 'scanoid/1.0' ? 5 : 5);
		var queryNearShop = 'SELECT ST_DISTANCE(shop_geom, ST_MakePoint(' + req.query.lon + ',' + req.query.lat +')) as distance, '+
		'shop_name as name, shop_descriptive as descriptive, shop_type as type, shop_house_num as numero, shop_road as rue, shop_postcode as postcode, '+
		'shop_town as town, shop_id as id, shop_longitude as longitude, shop_latitude as latitude '+
		'FROM shop WHERE shop_vectorise @@ plainto_tsquery(\''+ req.query.name +'\') ORDER BY distance ASC LIMIT ' + number + ' ;';
		pg(queryNearShop, function(err, result) {
			if (err) {
				res.json({registered : 'failure', err : [config.stringError.serverError]});
			} else {
				res.json({registered : 'success', results : result.rows});
			}
		});
	});
};

//Route GET <b>/shop/near/name</b><br />
// <small>route permettant de retrouver les magasins autour de la position de l'utilisateur qui contienne le produit</small><br />
// <small>route appelé dans <a href="jquery-map.html"><b>jquery-map.js</b></a></small> 
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>lon</b> : longitude de l'utilisateur</li>
//		<li><b>lat</b> : latitude de l'utilisateur</li>
//		<li><b>ean</b> : code EAN du produit recherché</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : résultats de la requête à postgreSQL</li>
//	</ul>
//</ul>
exports.getNearShopByProduct = function(config, pg, errorGes) {
	return (function (req, res) {
		var user = req.get("user-agent");
		var number = (user == 'scanoid/1.0' ? 5 : 5);
		var queryNearShop = 'SELECT ST_DISTANCE(x.shop_geom, ST_MakePoint(' + req.query.lon + ', ' + req.query.lat + ')) as distance, '+
		'x.*, y.price, y.ispromotion FROM shop x, product_shop y WHERE y.product_ean = \'' + req.query.ean + '\' AND '+
		'y.shop_id = x.shop_id AND y.insertdate IN (SELECT MAX(insertdate) FROM product_shop WHERE product_ean '+
		'= \'' + req.query.ean + '\' GROUP BY product_ean, shop_id) ORDER BY distance ASC LIMIT 5 ;';
		pg(queryNearShop, function(err, result) {
			if (err) {
				res.json({registered : 'failure', err : [config.stringError.serverError]});
			} else {
				res.json({registered : 'success', results : result.rows});
			}
		});
	});
};

//Route GET <b>/shop/near/name</b><br />
// <small>route permettant de retrouver les magasins autour des adresses favorites contenant le produit</small><br />
// <small>route appelé dans <a href="jquery-map.html"><b>jquery-map.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>lon</b> : longitude de l'utilisateur</li>
//		<li><b>lat</b> : latitude de l'utilisateur</li>
//		<li><b>ean</b> : code EAN du produit recherché</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : résultats de la requête à postgreSQL</li>
//	</ul>
//</ul>
exports.getNearShopByProductFavAddr = function(config, pg, errorGes) {
	return (function (req, res) {
		var user = req.get("user-agent");
		var number = (user == 'scanoid/1.0' ? 5 : 5);
		var queryNearShop =	'SELECT DISTINCT ON (shop_id) ST_DISTANCE(z.address_geom, x.shop_geom) as distance, x.*, y.price, '+
		'y.ispromotion FROM shop x, product_shop y, (SELECT address_geom FROM favorite_address WHERE '+
		'user_email = \'' + res.get('user') + '\') as z WHERE y.product_ean = \'' + req.query.ean +
		'\' AND y.shop_id = x.shop_id AND y.insertdate IN (SELECT MAX(insertdate) FROM product_shop WHERE '+
		'product_ean = \'' + req.query.ean + '\' GROUP BY product_ean, shop_id) ORDER BY shop_id, distance ASC LIMIT 5;';
		pg(queryNearShop, function(err, result) {
			if (err) {
				res.json({registered : 'failure', err : [config.stringError.serverError]});
			} else {
				res.json({registered : 'success', results : result.rows});
			}
		});
	});
};

//Route GET <b>/shop/near/name</b><br />
// <small>route permettant de retrouver les magasins faisant partie des magasins favoris contenant le produit</small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>lon</b> : longitude de l'utilisateur</li>
//		<li><b>lat</b> : latitude de l'utilisateur</li>
//		<li><b>ean</b> : code EAN du produit recherché</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : résultats de la requête à postgreSQL</li>
//	</ul>
//</ul>
exports.getNearShopByProductFavShop = function(config, pg, errorGes) {
	return (function (req, res) {
		var user = req.get("user-agent");
		var number = (user == 'scanoid/1.0' ? 5 : 5);
		var queryNearShop = 'SELECT x.*, y.price, y.ispromotion FROM '+
		'(SELECT x.* FROM shop x, favorite_shop y WHERE y.user_email = \'' + res.get('user') + '\' AND y.shop_id=x.shop_id)'+
		' as x, product_shop y WHERE y.product_ean = \'' + req.query.ean + '\' AND y.shop_id = x.shop_id '+
		'AND y.insertdate IN (SELECT MAX(insertdate) FROM product_shop WHERE product_ean = \'' + req.query.ean + '\' GROUP BY '+
		'product_ean, shop_id) LIMIT ' + number + ' ;';
		pg(queryNearShop, function(err, result) {
			if (err) {
				res.json({registered : 'failure', err : [config.stringError.serverError]});
			} else {
				res.json({registered : 'success', results : result.rows});
			}
		});
	});
};