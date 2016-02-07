//<table border="1">
//	<tr>
//		<th>route ajoutée dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Route GET <b>/logout</b><br />
// <small>route supprimant l'entrée dans la base de données Redis et les cookies de l'utilisateur lorsqu'il souhaite
// se déconnecter</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>document JSON</b> : confirmation de la déconnexion</li>
//		<li><b>page HTML</b> : home</li>
//	</ul>
//</ul>
exports.getLogout = function(config, client, errorGes) {
	return (function(req, res){
		var user = req.get("user-agent");
		client.del(req.signedCookies.name, function (err, resp) { // suppression de l'entré dans Redis
			if (err) {errorGes(user, res, 'login');}
			else if (user == 'scanoid/1.0') // envoi du document JSON pour l'application Android 
					res.json({registered : "success"});
			else {
				res.clearCookie('name'); // suppression du cookie pour l'utilisateur sur navigateur
				res.redirect('/'); // redirection vers home
			}
		});
	});
};