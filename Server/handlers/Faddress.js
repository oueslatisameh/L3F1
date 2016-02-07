//<table border="1">
//	<tr>
//		<th>routes ajoutées dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Route GET <b>/favorite/address</b><br />
// <small>route renvoyant les adresses favorites de l'utilisateur</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> contenant les informations pour l'application</li>
//		<li><b>Page HTML</b> : avec les informations pour le navigateur</li>
//	</ul>
//</ul>
exports.getFaddress = function getFaddress(config, pg, err) {
	return (function (req, res) {
		var user = req.get('user-agent');
		var queryFaddress = 'SELECT house_num, road, postcode, town, building, residential, suburb, town_district, county, ' +
		'state, longitude, latitude FROM favorite_address where user_email = \'' + res.get('user') + '\';';
		pg(queryFaddress, function(err, result) {
			if (err) {res.json({registered : "failure", err : [config.stringError.serverError]});}
			else if (user == 'scanoid/1.0') {
				res.json({registered : "success", results : result.rows});
			} else {
				res.render('favoriteaddress',  {
					web : config.web, 
					co : res.get("co"), 
					results : result.rows, 
					element : {
						house_num : "numéro",
						road : "rue",
						postcode : "code postal",
						town : "ville",
						suburb : "quartier",
						town_district : "arrondissement",
					}
				});
			}
		});
	});
};

//Route DELETE <b>/favorite/address:road-:town-:postcode</b><br />
// <small>route supprimant une adresse favorite</small><br />
// <small>route appelé dans <a href="jquery-faddress.html"><b>jquery-faddress.js</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>road</b> : nom de la rue</li>
//		<li><b>town</b> : nom de la ville</li>
//		<li><b>postcode</b> : code postal de l'addresse</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.delFaddress = function delFaddress(config, pg, err) {
	return (function (req, res) {
		var user = req.get('user-agent');
		req.params.road = req.params.road.replaceBadCharacter().trim().replace(/\+/g, "-");
		req.params.postcode = req.params.postcode.replaceBadCharacter().trim();
		req.params.town = req.params.town.replaceBadCharacter().trim();		
		var queryFaddress = 'DELETE FROM favorite_address where user_email = \'' + res.get('user') + '\'' + 
			' AND road = \'' + req.params.road + '\'' + 
			' AND postcode = \'' + req.params.postcode + '\'' + 
			' AND town = \'' + req.params.town + '\'' + 
			';';
		pg(queryFaddress, function(err, result) {
			if (err) {res.json({registered : "failure", err : [config.stringError.serverError]});}
			else {
				res.json({registered : "success"});
			}
		});
	});
};

//Route POST <b>/favorite/address</b><br />
// <small>route permettant la création d'une nouvelle addresse favorite</small><br />
// <small>route appelé dans <a href="jquery-faddress.html"><b>jquery-faddress.js</b></a></small>  
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>address</b> : nom de la rue avec le numéro</li>
//		<li><b>town</b> : nom de la ville</li>
//		<li><b>postcode</b> : code postal de l'addresse</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.postFaddress = function postFaddress(config, query_nominatim, pg,err) {
	return (function (req, res) {
		var user = req.get('user-agent');
		var epic = {
			road : req.body.address.replaceBadCharacter().trim(),
			town : req.body.town.replaceBadCharacter().trim(),
			postcode : req.body.postcode.replaceBadCharacter().trim(),
			email : res.get('user')
		};
		query_nominatim(epic, function(err, queryAddress) { //appel fait à nominatim pour avoir des informations sur la localisation
			if (!err) {
				pg(queryAddress, function(err, resultat) {
					if (err) {res.json({registered : "failure"});}
					else {res.json({registered : "success", results : epic});}
				});
			} else {res.json({registered : "failure"});}
		});
	});
};