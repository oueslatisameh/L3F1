//<b>app.js</b> est le coeur du serveur.
//<ul> 
// <li>les modules sont ajoutés avec la fonction require() de node.js</li>
// <li>les middlewares d'Express sont ajoutés à l'application</li>
// <li>les routes sont ajoutées à l'application Express</li>
//</ul> 

//<b>capitalize</b> : fonction prototype String<br />
// <i><small>Transforme le premier caractère en Majuscule</small></i>
String.prototype.capitalize = function() {
	return this.charAt(0).toUpperCase() + this.slice(1);
};

//<b>replaceBadCharacter</b> : fonction prototype String<br />
// <i><small>Remplace les caractères indésirables en espaces</small></i>
String.prototype.replaceBadCharacter = function() {
	return this.replace(/[_\'":;`|*^%$#!\(\)\[\]\{\}\/]/g, ' ');
};

//Ajout des modules node.js
//<ul>
//	<li>  <a href="http://expressjs.com/"><b>Express</b></a> : web application framework</li>
//	<li>  <a href="https://github.com/mranney/node_redis"><b>Redis</b></a> : client pour redis</li>
//	<li>  <a href="http://www.nodemailer.com/"><b>NodeMailer</b></a> : Envoi de mail</li>
//	<li>  <a href="https://github.com/brianc/node-postgres"><b>Pg</b></a> : client pour postgreSQL</li>
//</ul>  
var express = require('express');
var http = require('http');
var config = require('./config.json');
var redis = require("redis");
var app = express();
var request = require('./requete');
var pg = request.initPostPool(config);
var client = redis.createClient(config.redis.db_port, config.redis.db_host);
var functions = require('./schedule').startLaunch(config, pg, client);
var email = require("./email").email;
email.smtpTransport = email.funcSmtpTransport(config);

// Récupération des routes définies dans le dossier handlers
var home = require("./handlers/home");
var login = require("./handlers/login");
var logout = require("./handlers/logout");
var registration = require("./handlers/registration");
var account = require("./handlers/account");
var product = require("./handlers/product");
var card = require("./handlers/card");
var list = require("./handlers/list");
var Faddress = require("./handlers/Faddress");
var Fshop = require("./handlers/Fshop");
var shop = require("./handlers/shop");
var admin = require("./handlers/admin");
var recovery = require("./handlers/recovery");

//<b>err</b> : module pour envoyer des erreurs selon le client
//<ul>
//	<li>paramètres</li>
//	<ul>
//		<li><b>user</b> : user-agent du client</li>
//		<li><b>res</b> : objet réponse de la requête serveur</li>
//		<li><b>page</b> : URL de redirection</li>
//		<li><b>error</b> : tableau des erreurs</li>
//	</ul>
//	<li>sortie</li>
//  <ul>
//		<li><b>document JSON</b> pour l'application</li>
//		<li><b>page HTML</b> pour le navigateur</li>
//	</ul>  
//</ul>
function err(user, res, page, error) {
	if (!error) {
		error = [config.stringError.serverError];
	}
	console.log(error);
	if (user == 'scanoid/1.0') {
		res.json({registered : "failure", err : error});
	} else {
		res.render(page, {web : config.web, err : error});
	}
}

//<b>ensureAuthenticated</b> : middleware de sécurité
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>req</b> : objet requête de la requête serveur</li>
//		<li><b>res</b> : objet réponse de la requête serveur</li>
//		<li><b>next</b> : prochaine route</li>
//	</ul>
//	<li>sortie</li>
// 	<ul>
//		<li>lancement de la route</li>
//		<li>err : redirection vers home pour le navigateur</li>
//		<li>err : document JSON contenant le détail de l'erreur pour l'application</li>
//	</ul>
//</ul>
function ensureAuthenticated(req, res, next) {
	if (!req.signedCookies.name) {
		res.render('home', {web : config.web});
	} else {
		client.get(req.signedCookies.name, function (err, resp) {
			if (!err) {
				if (resp !== null) {
					res.set("co", true);
					res.set("user", resp);
					next();
				} else if (req.get("user-agent") == 'scanoid/1.0')
					res.json({registered : "failure", err : [config.stringError.ServerError]});
				else
					res.redirect('/login');
			} else if (req.get("user-agent") == 'scanoid/1.0')
					res.json({registered : "failure", err : [config.stringError.ServerError]});
			else
				res.redirect('/login');
		});
	}
}

//Ajout des middlewares Express
//<ul>
//	<li><b>use(express.static("./public"))</b> : définition du dossier contenant le CSS, Js côté client, les images... </li>
//	<li><b>set("view", __dirname + "views")</b> : définition du dossier contenant les vues du serveur</li>
//	<li><b>set("view engine", "jade")</b> : définition du template engine par défaut</li>
//	<li><b>cookieParser</b> : permettre l'utilisation des cookies</li>
//	<li><b>bodyParser</b> : parse les champs des requêtes POST</li>
//	<li><b>methodOverride</b> : permet la réception de requête de type DELETE, PUT</li>
// <li><b>responseTime</b> : mise à jour du temps de service du serveur dans la réponse</li>
//	<li><b>errorHandler</b> : retourne une page HTML dans le cas où l'URL n'existe pas</li>
//</ul>
app.use(express.static('./public'));
app.set('views', __dirname + '/views');
app.set('view engine', 'jade');
app.use(express.favicon(__dirname + '/public/img/blob.ico'));
app.use(express.cookieParser(config.config.secret));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.responseTime());
app.use(express.errorHandler());
app.locals.pretty = true;

// Routes définies dans le fichier <a href="registration.html"><b>registration.js</b></a>
app.get('/registration', registration.getRegistration(config));
app.post('/registration', registration.postRegistration(config, request.queryRegistration(client), pg, email, err));
app.get('/email', registration.getEmail(config, pg, err));

// Routes définies dans le fichier <a href="login.html"><b>login.js</b></a>
app.get('/login', login.getLogin(config));
app.get('/login/:id', login.getLoginId(config, request.queryLoginId(client), 
	request.queryNominatim(http, config), request.queryStatIncr(client), pg, err));
app.post('/login', login.postLogin(config, client, request.queryStatIncr(client), pg, err));
app.post('/loginFacebook', login.postLoginFacebook(config, client, pg, err));

app.post('/recovery', recovery.postRecovery(config, request.queryRegistration(client), pg, email, err));
app.get('/recovery/:id', recovery.getRecovery(config, request.queryLoginId(client), pg, err));

app.get('/connected', function(req, res) {
	if (!req.signedCookies.name) {
		res.json({registered : "failure"});
	} else {
		client.get(req.signedCookies.name, function (err, resp) {
			console.log(resp + " " + typeof resp);
			if (!err && resp) {
				res.json({registered : "success"});
			} else {
				res.json({registered : "failure"});
			}
		});
	}
});

// Routes définies dans le fichier <a href="home.html"><b>home.js</b></a>
app.get('/(home)?', ensureAuthenticated, home.getHome(config));

// Routes définies dans le fichier <a href="logout.html"><b>logout.js</b></a>
app.get('/logout', ensureAuthenticated, logout.getLogout(config, client, err));

// Routes définies dans le fichier <a href="account.html"><b>account.js</b></a>
app.get('/account', ensureAuthenticated, account.getAccount(config, pg, err));
app.post('/account', ensureAuthenticated, account.postAccount(config, pg, err));

// Routes définies dans le fichier <a href="product.html"><b>product.js</b></a>
app.get('/product', ensureAuthenticated, product.getProduct(config, 
	request.queryStatIncr(client), pg, err));
app.get('/productnext', ensureAuthenticated, product.getProductNext(config, pg, err));
app.get('/productname', ensureAuthenticated, product.getProductName(config, pg, err));
app.post('/product/:product_ean/shop/:shop_id/price/:price-:promotion', ensureAuthenticated,
	product.postProductPrice(config, request.queryProductPrice(config, client, pg), 
							 request.queryStatIncr(client), pg, err));


// Routes définies dans le fichier <a href="card.html"><b>card.js</b></a>
app.get('/card', ensureAuthenticated, card.getCard(config, pg, err));
app.del('/card/:brand', ensureAuthenticated, card.delCard(config, pg, err));
app.post('/card/:brand-:code', ensureAuthenticated, card.postCard(config, pg, err));
app.get('/brand', ensureAuthenticated, card.getBrand(config, pg, err));
app.put('/card/:brand-:code', ensureAuthenticated, card.putCard(config, pg, err));

// Routes définies dans le fichier <a href="list.html"><b>list.js</b></a>
app.get('/list', ensureAuthenticated, list.getList(config, pg, err));
app.post('/list/:name', ensureAuthenticated, list.postList(config, pg, err));
app.del('/list/:name', ensureAuthenticated, list.delList(config, pg, err));
app.put('/list/:old-:ne', ensureAuthenticated, list.putList(config, pg, err));
app.post('/list/:name/product/:product', ensureAuthenticated, list.postListProduct(config, pg, err));
app.del('/list/:name/product/:product', ensureAuthenticated, list.delListProduct(config, pg, err));
app.get('/listname', ensureAuthenticated, list.getListName(config, pg, err));
app.post('/list/:name/product/:product/amount/:amount', ensureAuthenticated, list.postAmount(config, pg, err));

// Routes définies dans le fichier <a href="Faddress.html"><b>Faddress.js</b></a>
app.get('/favorite/address', ensureAuthenticated, Faddress.getFaddress(config, pg, err));
app.del('/favorite/address/:road-:town-:postcode', ensureAuthenticated, Faddress.delFaddress(config, pg, err));
app.post('/favorite/address', ensureAuthenticated, Faddress.postFaddress(config, request.queryNominatim(http, config), pg, err));

// Routes définies dans le fichier <a href="Fshop.html"><b>Fshop.js</b></a>
app.get('/favorite/shop', ensureAuthenticated, Fshop.getFshop(config, pg, err));
app.del('/favorite/shop/:shop_id', ensureAuthenticated, Fshop.delFshop(config, pg, err));
app.post('/favorite/shop/:shop_id', ensureAuthenticated, Fshop.postFshop(config, pg, err));

// Routes définies dans le fichier <a href="shop.html"><b>shop.js</b></a>
app.get('/shop/near', ensureAuthenticated, shop.getNearShop(config, pg, err));
app.get('/shop/near/name', ensureAuthenticated, shop.getNearShopByName(config, pg, err));
app.get('/shop/near/product', ensureAuthenticated, shop.getNearShopByProduct(config, pg, err));
app.get('/shop/near/productFavAddr', ensureAuthenticated, shop.getNearShopByProductFavAddr(config, pg, err));
app.get('/shop/near/productFavShop', ensureAuthenticated, shop.getNearShopByProductFavShop(config, pg, err));

app.get('/admin', ensureAuthenticated, admin.system(config, pg, client));
app.get('/admin/selectfile', ensureAuthenticated, admin.selectFile(config));
app.get('/admin/sendfile', ensureAuthenticated, admin.sendFile(config));
app.post('/admin/receivefile', ensureAuthenticated,admin.receiveFile(config));
app.get('/admin/process', ensureAuthenticated,admin.systemProcess(config, pg, client));
app.get('/admin/activity', ensureAuthenticated,admin.systemActivity(config, pg, client));

app.get('/about', ensureAuthenticated, require('./handlers/about').getAbout(config));

app.get('/test', ensureAuthenticated, function(req, res) {
	res.json({registered : config.admin});
});

// Démarrage du serveur sur le port spécifié dans le fichier de configuration config.json
http.createServer(app).listen(config.config.port, function(){
	console.log(config.config.app + ' started');
});
