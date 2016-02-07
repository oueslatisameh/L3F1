//<table border="1">
//	<tr>
//		<th>routes ajoutées dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//<b>isEmpty</b> : fonction vérifiant si un objet est nulle ou non
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>obj</b> : un objet quelconque</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>true</b> si l'objet est vide (ex: "", [], {})</li>
//		<li><b>false</b> si l'objet n'est pas vide</li>
//	</ul>
//</ul>
function isEmpty(obj) {
  if (obj === null) return true;
  if (obj.length > 0)    return false;
  if (obj.length === 0)  return true;
	for (var key in obj) {
    if (hasOwnProperty.call(obj, key)) return false;
  }
	return true;
}

//<b>fillResponse</b> : fonction remplissant un objet avec le résultat de la requête dans getList
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>resultat</b> : un objet contenant les noms des listes</li>
//		<li><b>result</b> : un objet contenant les tuples de la requête à postgreSQL</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>results</b> objet rempli de produit trié par liste</li>
//	</ul>
//</ul>
function fillResponse(resultat, result) {
	var results = {number : 0, lists : []};
	var list = null;
	for (i = 0; i < resultat.rows.length; ++i) {
		results.number++;
		results.lists.push({title : resultat.rows[i].list_name, number : 0, products : []});
		list = (list === null) ? results.lists[0] : list;
	}
	for (i = 0; i < result.rows.length; ++i) {
		if (list.title != result.rows[i].list_name) {
			for (o = 0; o < results.lists.length; ++o) {
				if (results.lists[o].title == result.rows[i].list_name)
					list = results.lists[o];
			}
		}
		list.number++;
		list.products.push({name : result.rows[i].product_name,
			img : result.rows[i].product_img,
			amount : result.rows[i].amount,
			ean : result.rows[i].product_ean,
			bname : result.rows[i].brand_name,
			bsin : result.rows[i].brand_sin,
			descriptive : result.rows[i].product_descriptive});
	}
	return (results);
}

//Route GET <b>/list</b><br />
// <small>route renvoyant les listes de courses</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> contenant les informations pour l'application</li>
//		<li><b>Page HTML</b> : avec les informations pour le navigateur</li>
//	</ul>
//</ul>
exports.getList = function (config, pg, errorGes) {
	return (function(req, res) {
		var user = req.get("user-agent");
		var queryLists = 'SELECT list_name FROM list WHERE user_email = \'' + res.get('user') + '\';';
		pg(queryLists, function(err, resultat) {
			if (err) {errorGes(user, res, 'login');}
			else {
				var queryList = 'SELECT x.list_name, \'gtin-\' || left(x.product_ean, 3) as product_img, ' +
				'y.product_ean, y.product_name, y.product_descriptive, z.brand_name, z.brand_sin, x.amount FROM list_product x, product y, brand z WHERE x.user_email = \'' +
				res.get('user') + '\' AND x.product_ean = y.product_ean AND y.brand_sin = z.brand_sin'+
				' ORDER BY x.list_name ASC;';
				pg(queryList, function(err, result) {
					if (err) {errorGes(user, res, 'login');}
					else {
						var results = fillResponse(resultat, result);
						if (user == 'scanoid/1.0') {
							res.json({registered : "success", results : results});
						} else {
							res.render('list', {web : config.web, co : true, results : results});
						}
					}
				});
			}
		});
	});
};

//Route POST <b>/list/:name</b><br />
// <small>route permettant la création d'une nouvelle liste de courses</small><br />
// <small>route appelé dans <a href="jquery-list.html"><b>jquery-list.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la nouvelle liste</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.postList = function(config, pg, errorGes) {
	return (function(req, res) {
		req.params.name = req.params.name.replaceBadCharacter().trim();
		var user = req.get("user-agent");
		var queryListProduct = 'INSERT INTO list VALUES(\''+ req.params.name +'\',\'' + res.get('user')+ '\');';
		pg(queryListProduct, function(err, result) {
			if (err) {
				res.json({registered : 'failure', err : ['a card have the same name']});
			} else {
				res.json({registered : 'success'});
			}
		});
	});
};

//Route DELETE <b>/list/:name</b><br />
// <small>route supprimant une liste de courses</small><br />
// <small>route appelé dans <a href="jquery-list.html"><b>jquery-list.js</b></a></small> 
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la liste</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.delList = function(config, pg, errorGes) {
	return (function(req, res) {
		req.params.name = req.params.name.replaceBadCharacter().trim();
		var user = req.get("user-agent");
		var queryListProduct = 'DELETE from list where user_email = \'' + res.get('user') +
			'\' AND list_name = \'' + req.params.name + '\';';
		pg(queryListProduct, function(err, result) {
			if (err) {
				res.json({registered : 'failure', err : ['no list with this name']});
			} else {
				res.json({registered : 'success'});
			}
		});
	});
};

//Route PUT <b>/list/:old-:ne</b><br />
// <small>route permettant de changer le nom d'une liste de courses</small><br />
// <small>route appelé dans <a href="jquery-list.html"><b>jquery-list.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>old</b> : ancien nom</li>
//		<li><b>ne</b> : nouveau nom</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.putList = function(config, pg, errorGes) {
	return (function(req, res) {
		req.params.old = req.params.old.replaceBadCharacter().trim();
		req.params.ne = req.params.ne.replaceBadCharacter().trim();
		var user = req.get('user-agent');
		var queryList = 'UPDATE list SET list_name = \'' +
		req.params.ne + '\' where user_email = \'' +
		res.get('user') + '\' AND list_name = \'' + req.params.old + '\';';
		pg(queryList, function(err, result) {
			if (err) {res.json({registered : "failure", err : [config.stringError.serverError]});}
			else {
				res.json({registered : "success"});
			}
		});
	});
};

//Route GET <b>/listname</b><br />
// <small>route renvoyant les noms des listes</small><br />
// <small>route appelé dans <a href="jquery-product.html"><b>jquery-product.js</b></a></small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> contenant les informations pour l'application et le navigateur</li>
//	</ul>
//</ul>
exports.getListName = function(config, pg, errorGes) {
	return (function(req, res) {
		var user = req.get("user-agent");
		var queryLists = 'SELECT list_name as name FROM list WHERE user_email = \'' + res.get('user') + '\';';
		pg(queryLists, function(err, result) {
			if (err) {res.json({registered : "failure", err : ['no lists']});}
			else
				res.json({registered : "success", results : result.rows});
		});
	});
};

//Route POST <b>/list/:name/product/:product</b><br />
// <small>route permettant de mettre un produit dans une liste</small><br />
// <small>route appelé dans <a href="jquery-product.html"><b>jquery-product.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la liste</li>
//		<li><b>product</b> : code EAN du produit</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.postListProduct = function(config, pg, errorGes) {
	return (function(req, res) {
		req.params.name = req.params.name.replaceBadCharacter().trim();
		var user = req.get("user-agent");
		var queryListProduct = 'INSERT INTO list_product VALUES(\'' +
		req.params.name + '\',\'' + res.get('user') + '\',\'' + req.params.product + '\')';
		pg(queryListProduct, function(err, result) {
			if (err) {
				res.json({registered : 'failure', err : ['product with this ean already exists']});
			} else {
				res.json({registered : 'success'});
			}
		});
	});
};

//Route DELETE <b>/list/:name/product/:product</b><br />
// <small>route supprimant un produit dans une liste</small><br />
// <small>route appelé dans <a href="jquery-list.html"><b>jquery-list.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la liste</li>
//		<li><b>product</b> : code EAN du produit</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.delListProduct = function(config, pg, errorGes) {
	return (function(req, res) {
		req.params.name = req.params.name.replaceBadCharacter().trim();
		var user = req.get("user-agent");
		var queryListProduct = 'DELETE from list_product where user_email = \'' + res.get('user') +
		'\' AND list_name = \'' + req.params.name + '\' AND product_ean = \'' + req.params.product + '\';';
		pg(queryListProduct, function(err, result) {
			if (err) {
				res.json({registered : 'failure', err : ['no product with this ean']});
			} else {
				res.json({registered : 'success'});
			}
		});
	});
};

//Route POST <b>/list/:name/product/:product/amount/:amount</b><br />
// <small>route permettant de changer le nombre d'un produit dans une liste</small><br />
// <small>route appelé dans <a href="jquery-list.html"><b>jquery-list.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la liste</li>
//		<li><b>product</b> : code EAN du produit</li>
//		<li><b>product</b> : nombre de produit</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.postAmount = function(config, pg, errorGes) {
	return (function(req, res) {
		req.params.name = req.params.name.replaceBadCharacter();
		if (req.params.amount < 0 || req.params.amount > 50)
			res.json({registered : "failure"});
		else {
			var queryAmount = 'UPDATE list_product SET amount = ' + req.params.amount +
			' WHERE list_name = \''+ req.params.name +'\' AND product_ean = \'' + req.params.product +
			'\' AND user_email = \'' + res.get('user') + '\';';
			pg(queryAmount, function(err, result) {
				if (err) {res.json({registered : "failure"});}
				else {res.json({registered : "success"});}
			});
		}
	});
};