var nuance = [
	"email",
	"name",
	"forename"
];

function test() {
	help = $("input#inputhidden");
	if ($("label#labelhidden").html() === "birthdate" && /[0-9]{2}[\/-][0-9]{2}[\/-][0-9]{4}/.test(help.val())) {
		var tab;
		tab = help.val().split((help.val().indexOf('/') == -1) ? "-" : "/");	
		help.val(tab[2] + "+" + tab[1] + "+" + tab[0]);
	} else if ($("label#labelhidden").html() === "address") {
		help.val().replace(/-/g, "+");
	} else if ($("label#labelhidden").html() === "password") {
		if (help.val() ===  help.val()) {
			help.val(CryptoJS.SHA1(help.val()));
		} else {
			$("div#jumb").prepend(
				$("<div class=\"alert alert-danger alert-dismissable\"></div>")
					.html("les deux mots de passe sont differents")
					.prepend(
						$("<button type=\"button\" data-dismiss=\"alert\" aria-hidden=\"true\" class=\"close\"></button>")
							.html("&times;")
					)
			);
			return;
		}
	}
	$.ajax({
		url : '/account/',
		type : 'POST',
		data : {
			field : $("label#labelhidden").html(),
			value : help.val().replace(/ /g, "_").replace(/-/g, '+')
		},
		dataType: "json",
		complete: function(xhr, result) {
			json = JSON.parse(xhr.responseText);
			if (result == 'success' && json.registered == 'success') {
				document.location.reload(true);
			} else {
				$("span#spanhidden").html(json.err[0]);
				help.val("");
				$("div#jumb").prepend(
					$("<div class=\"alert alert-danger alert-dismissable\"></div>")
						.html(json.err[0])
						.prepend(
							$("<button type=\"button\" data-dismiss=\"alert\" aria-hidden=\"true\" class=\"close\"></button>")
								.html("&times;")
						)
				);
			}
		}
	});
}

function change(name) {
	$('#labelhiddenCon').css("display", "none");
	$('input#inputhiddenCon').css("display", "none");
	if($.inArray(name, nuance) != -1) {
		$('div#containerhidden').css("display", "none");
	} else {
		$('div#containerhidden').css("display", "block");
		$('label#labelhidden').html(name);
		if (name == 'birthdate')
			$("input#inputhidden").attr("type", "date");
		else if (name == 'password') {
			$("input#inputhidden").attr("type", "password");
			$('#labelhiddenCon').css("display", "inline");
			$('input#inputhiddenCon').css("display", "inline");
		} else {
			$("input#inputhidden").attr("type", "text");
		}
		$("input#inputhidden").attr("name", name).attr("placeholder", name);
	}
}

$('#labelhiddenCon').css("display", "none");
$('input#inputhiddenCon').css("display", "none");