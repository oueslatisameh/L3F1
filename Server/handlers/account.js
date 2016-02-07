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
var crypto = require('crypto');

// Déclaration de tableau 
var nuance = [ // tableau des champs non modifiables par l'utilisateur
	"email",
	"name",
	"forename"
];

var nu = { // tableau des champs d'un champ avec sa fonction de format et si le champ est clé primaire dans postgreSQL ou non
	email : {val :validator.isEmail, ne : true},
	password : {val : function() {return(true);}, ne : true},
	cpassword : {val : function() {return(true);}, ne : true},
	name : {val : validator.isAlpha, ne : true},
	forename : {val : validator.isAlpha, ne : false},
	birthdate : {val : validator.isDate, ne : false},
	address : {val : function (input) {
		input = input.replace(/[, áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ-]/g, "");
		return (validator.isAlphanumeric(input));
		}, ne : false},
	postcode : {val : validator.isAlphanumeric, ne : false},
	town : {val : function (input) {
		input = input.replace(/[, áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ-]/g, "");
		return (validator.isAlphanumeric(input));
		}, ne : false},
};

//Route GET <b>/account</b><br />
// <small>route renvoyant les informations sur l'utilisateur</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> contenant les informations pour l'application</li>
//		<li><b>Page HTML</b> : avec les informations pour le navigateur</li>
//	</ul>
//</ul>
exports.getAccount = function(config, pg, errorGes) {
	return (function(req, res){
		var user = req.get("user-agent");
		var queryAccount = "SELECT user_name as name, user_forename as forename, user_email as email, " + // création de la requête
			"user_birthdate as birthdate, user_address as address, user_postcode as postcode, user_town as town" + 
			" FROM users where user_email = \'" + res.get('user') + "\';";
		pg(queryAccount, function(err, result) { // envoit de la requête à postgreSQL
			if (err) {errorGes(user, res, 'login');}
			else if (user == 'scanoid/1.0') // renvoit d'une réponse selon le client
				res.json({registered : "success", account : result.rows[0]});
			else
				res.render('account', {web : config.web, co : res.get('co'), results : result.rows[0]});
		});
	});
};

//Route POST <b>/account/:field-:value</b><br />
// <small>route qui modifie les champs d'un utilisateur et renvoit confirmation</small> 
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>field</b> : nom du champ à modifier</li>
//		<li><b>value</b> : nouvelle valeur du champ</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la requête</li>
//	</ul>
//</ul>
exports.postAccount = function(config, pg, errorGes) {
	return (function(req, res) {
		if (req.body.field === 'password') {
			req.body.value = crypto.createHash('sha1').update(req.body.value).digest('hex');
		}
		req.body.value = req.body.value.replaceBadCharacter().replace(/\+/g, '-').trim();
		var user = req.get('user-agent');
		var error = null;
		
		for (i = 0; i < nuance.length; ++i) { //vérification du champ envoyé par le client
			if (nuance[i] == req.body.field) {
				error = nuance[i] + " ne peut pas etre modifie";
			}
		}
		if (!nu[req.body.field]) {
			error = "parametre inconnu";
		} else if (!nu[req.body.field].val(req.body.value)) {
			error = config.stringRL['form' + req.body.field.capitalize()];
		}
		if (error === null) { // si pas d'erreur
			var queryAccount = 'UPDATE users SET user_' + req.body.field + // création de la requête 
				' = \'' + req.body.value + '\' where user_email = \'' + res.get('user') + '\';';
			pg(queryAccount, function(err, result) { // envoit de la requête à postgreSQL
				if (err) {res.json({registered : "failure", err : [config.stringError.serverError]});}
				else {
					res.json({registered : "success"}); // renvoit de la confirmation
				}
			});
		}  else {
			res.json({registered : "failure", err : [error]});
		}
	});
};