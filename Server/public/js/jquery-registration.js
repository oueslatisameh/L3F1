//<table border="1">
//	<tr>
//		<th>contenu dans la page </th> <th><b>registration.jade</b></th>
//	</tr>
//</table>

// Déclaration de tableau
var nuance = {// tableau des champs d'un champ avec sa fonction de format et si le champ est clé primaire dans postgreSQL ou non
	email : {val :validator.isEmail, ne : true, name : "Addresse Email"},
	password : {val : function() {return(true);}, ne : true, name : "Mot de passe"},
	cpassword : {val : function() {return(true);}, ne : true, name : "Confirmation"},
	name : {val : validator.isAlpha, ne : true, name : "Nom"},
	forename : {val : validator.isAlpha, ne : true, name : "Prenom"},
	birth : {val : function(input) {
		if (/[0-9]{2}[\/-][0-9]{2}[\/-][0-9]{4}/.test(input)) {
			return(true);
		} else if (/[0-9]{4}-[0-9]{2}-[0-9]{2}/.test(input)) {
			return(true);
		}
		return (false);
	}, ne : false, name : "Date de Naissance"},
	address : {val : function (input) {
			input = input.replace(/[, áàâäãåçéèêëíìîïñóòôöõúùûüýÿæœÁÀÂÄÃÅÇÉÈÊËÍÌÎÏÑÓÒÔÖÕÚÙÛÜÝŸÆŒ-]/g, "");
			return (validator.isAlphanumeric(input));
		}, ne : false, name : "Adresse"},
	postcode : {val : validator.isNumeric, ne : false, name : "Code postal"},
	town : {val : validator.isAlpha, ne : false, name : "Ville"}
};


//<b>verify</b> : lorsque l'utilisateur souhaite soumettre le formule d'inscription, les champs sont vérifiés,
// modifiés dans le cas de la date de naissance pour correspondre au format de postrgeSQL et supprimés si
// les fonctions de validation retournent false. Lorsque les champs obligatoires ont été entré et que
// le format des autres sont correct, une requête AJAX est faite au serveur avec les valeurs des champs<br />
// <small>fais appel à la route DELETE <a href="registration.html"><b>/email</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom de la marque</li>
//		<li><b>i</b> : indice de la carte</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : rechargement de la page</li>
//		<li><b>failure</b> : indication à l'utilisateur que la carte n'existe pas</li>
//	</ul>
//</ul>
function verify() {
	var fault = false;
	var err = [];
	var help;
	for (var i in nuance) { // vérification des champs avec le tableau nuance
		help = $("input[name='" + i + "']");
		if (nuance[i].ne && help.val() === "") {
			fault = true;
			help.val("");
			err.push(nuance[i].name + " n'a pas ete entre(e)");
		} else if (help.val() !== "" && !nuance[i].val(help.val())) {
			fault = true;
			err.push(nuance[i].name + " contient des caracteres illegaux");
			help.val("");
		}
	}
	if ($("input[name='password']").val() != $("input[name='cpassword']").val()) { // si les deux mot de passe sont différent
		fault = true;
		err.push("Les mots de passe sont differents");
		$("input[name='cpassword']").val("");
	}
	help = $("input[name='birth']");
	if (help.val() !== "" && /[0-9]{2}[\/-][0-9]{2}[\/-][0-9]{4}/.test(help.val())) { // formatage de la date de naissance
		var tab;
		tab = help.val().split((help.val().indexOf('/') == -1) ? "-" : "/");	
		help.val(tab[2] + "-" + tab[1] + "-" + tab[0]);
	}
	if (fault) { // en cas d'erreur
		$("div#jumb").prepend( // ajout d'un message d'erreur dans la page web avec des indications
				$("<div class=\"alert alert-warning alert-dismissable\"></div>").html((function() {
					var result = "";
					for (y = 0; y < err.length; y++) {
						result += err[y] + "<br />";
					}
					return (result);
				})()).prepend(
						$("<button type=\"button\" data-dismiss=\"alert\" aria-hidden=\"true\" class=\"close\"></button>")
						.html("&times;")
				)
		);
		$('html, body').animate({ scrollTop: 0 }, 'slow'); // positionne le navigateur en haut de page
	} else {
		$.ajax({ // requête AJAX au serveur
			url : '/email/',
			type : 'GET',
			dataType : "json",
			data : 'email=' + $("input[name='email']").val(), // vérification de la disponibilité de l'adresse email
			complete : function(xhr, result) {
				json = JSON.parse(xhr.responseText);
				if (result == 'success' && json.registered == 'success') { // si oui alors hashage du mot de passe
					$("input[name='password']").val(CryptoJS.SHA1($("input[name='password']").val()));
					$("input[name='cpassword']").val(CryptoJS.SHA1($("input[name='cpassword']").val()));
					$("form#forr").submit(); // envoie des informations
				} else { // sinon
					$("input[name='email']").val("");
					$("div#jumb").prepend(// ajout d'un message d'erreur dans la page web avec une indication
						$("<div class=\"alert alert-warning alert-dismissable\"></div>")
							.html("l'adresse email existe deja")
							.prepend(
								$("<button type=\"button\" data-dismiss=\"alert\" aria-hidden=\"true\" class=\"close\"></button>")
									.html("&times;")
							)
					);
					$('html, body').animate({ scrollTop: 0 }, 'slow');  // positionne le navigateur en haut de page
				}
			}
		});
	}
}

// <b>reset</b> : supprime le contenu de tout les champs. lancé lorsque l'utilisateur appui sur rafraîchir
function reset() {
	for (var i in nuance) {
		help = $("input[name='" + i + "']");
		help.val("");
	}
}