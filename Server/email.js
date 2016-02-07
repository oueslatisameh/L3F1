//<b>email.js</b> contient les fonctions en rapport avec l'envoi d'emails.
//<ul> 
// <li>configuration du client smtp</li>
// <li>création du contenu d'un email</li>
//</ul>

//<table border="1">
//	<tr>
//		<th>module appelé dans</th> <th><a href="registration.html"><b>registration.js</b></a></th>
//	</tr>
//</table>

//Ajout des modules node.js
//<ul>
// <li>  <a href="http://www.nodemailer.com/"><b>NodeMailer</b></a> : Envoi de mail</li>
//</ul> 
var nodemailer = require("nodemailer");

//<b>smtpTransport</b> : objet de configuration de lanceur d'email
var funcSmtpTransport = function (config) {
	return (
		nodemailer.createTransport("SMTP",{
			service : "Gmail",
			auth : {
				user: config.email.mail_address,
				pass: config.email.mail_password
			}
		})
	);
}

//<b>Email</b> : fonction qui renvoit le contenu d'un email sous forme d'objet
var Email = function (email, urldev, buf) {
	return({
		from : "ScanOID <application.scanoid@gmail.com>",
		to : email,
		subject : "Inscription sur ScanOID",
		text : "blabla",
		html : "<h1>Inscription a ScanOID</h1><br />" +
		"<p> veuillez cliquer sur le lien en dessous : "+
		"<a href=\"http://" + urldev + "/login/" + buf +
		"\">Lien vers le site ScanOID</a></p>"
	});
};
 
// création du module avec smtpTransport et Email
exports.email = {
	funcSmtpTransport : funcSmtpTransport,
	Email : Email
};