var myWindow;
var fileName = "";
var loadFileName = "";
var sysProcessCPU;
var sysProcessMEM;
var sysActivity;

// création de l'éditeur de textes dans la balise textarea 
editAreaLoader.init({
	id : "configTextArea", // id of the textarea to transform
	start_highlight : true,
	font_size : "11",
	font_family : "verdana, monospace",
	allow_resize : "y",
	allow_toggle : false,
	language : "fr",
	syntax : "js",
	toolbar : "save, load, |, charmap, fullscreen, |, search, undo, redo, |, change_smooth_selection, highlight, reset_highlight",
	load_callback : "my_load",
	save_callback : "my_save",
	plugins : "charmap",
	charmap_default : "arrows",
	replace_tab_by_spaces : 2,
});

//<b>my_save</b> : fais une requête AJAX pour sauvegarder le fichier modifier dans l'éditeur de texte<br />
// <small>fais appel à la route POST <a href="admin.html"><b>/admin/receivefile</b></a></small>
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>id</b> : identifiant du fichier</li>
//		<li><b>content</b> : contenu de ce fichier</li>
//	</ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : affichage d'un confirmation</li>
//		<li><b>failure</b> : affichage de l'erreur renvoyée par le serveur</li>
//	</ul>
//</ul>
function my_save(id, content) {
	$.ajax({
		url : '/admin/receivefile/',
		type : 'POST',
		data : {
			name : (loadFileName || "NewDocument"),
			file : content
		},
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText); // parse de la réponse
			if (result == 'success' && json.registered == 'success') {
				alert("sauvegarde de " + loadFileName + " !");
			} else {
				alert(json.err);
			}
		}
	});
}

//<b>changefile</b> : lorsque l'utilisateur a selectionné un fichier, il est mis dans la variable fileName<br />
//<ul>
//	<li>paramètre</li>
//	<ul>
//		<li><b>name</b> : nom du fichier</li>
//	</ul>
//</ul>
function changefile(name) {
	fileName = name;
}

//<b>loadFileInTextArea</b> : récupère avec une requête AJAX le contenu d'un fichier et le met dans le textarea<br />
// <small>fais appel à la route GET <a href="admin.html"><b>/admin/sendfile</b></a></small>
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : affichage dans l'éditeur de texte du fichier</li>
//	</ul>
//</ul>
function loadFileInTextArea() {
	$("div#myModal").modal('hide');
	$.ajax({
		url : '/admin/sendfile/',
		type : 'GET',
		data : 'name=' + fileName,
		dataType : (fileName.indexOf('.json') != -1) ? "json" : "plain/text",
		complete : function(xhr, result) {
			$('textarea#configTextArea').html(xhr.responseText);
			editAreaLoader.setValue("configTextArea", xhr.responseText);
			loadFileName = fileName;
		}
	});
}

//<b>my_load</b> : fais une requête AJAX pour récuperer les noms de fichier disponibles pour la modification<br />
// <small>fais appel à la route GET <a href="admin.html"><b>/admin/selectfile</b></a></small>
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : affichage d'une fenêtre modal avec les fichiers</li>
//	</ul>
//</ul>
function my_load(id) {
	$.ajax({
		url : '/admin/selectfile/',
		type : 'GET',
		dataType : "text/html",
		complete : function(xhr, result) {
			$('div#myModalListGroup').html(xhr.responseText);
			$("div#myModal").modal('show');
		}
	});
}

//<b>reloadProcess</b> : récupération depuis le serveur des informations sur les processus et création des graphiques
// (utilisation du CPU et de la mémoire)<br />
// <small>fais appel à la route GET <a href="admin.html"><b>/admin/process</b></a></small>
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : affichage des deux graphiques</li>
//	</ul>
//</ul>
function reloadProcess() {
	$.ajax({
		url : '/admin/process/',
		type : 'GET',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText); // parse de la réponse
			if (result == 'success' && json.registered == 'success') {
				var dataCPU = [
					{value : json.results.node.cpu || 1, color: "#8CC84B"},
					{value : json.results.redis.cpu || 1, color: "#A41A11"},
					{value : json.results.postgresql.cpu || 1, color: "#196984"},
					{value : 100 - json.results.node.cpu - json.results.redis.cpu - json.results.postgresql.cpu || 1, 
						color: "#A0A0A0"}
				];
				new Chart(sysProcessCPU).Doughnut(dataCPU);  // création du graphiques dans la balise canvas
				var dataMEM = [
					{value : json.results.node.memory || 1,color: "#8CC84B"},
					{value : json.results.redis.memory || 1,color: "#A41A11"},
					{value : json.results.postgresql.memory || 1, color: "#196984"},
					{value : 100 - json.results.node.memory - json.results.redis.memory - json.results.postgresql.memory || 1,
						color: "#A0A0A0"}
				];
				new Chart(sysProcessMEM).Doughnut(dataMEM);  // création du graphiques dans la balise canvas
			}
		}
	});
}

//<b>reloadProcess</b> : récupération depuis le serveur des informations sur les activités du serveur pour en faire
// un graphique et lancement de la fonction reloadProcess pour les processus<br />
// <small>fais appel à la route GET <a href="admin.html"><b>/admin/activity</b></a></small>
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>success</b> : affichage des deux graphiques</li>
//	</ul>
//</ul>
function init() {
	sysProcessCPU = $("canvas#processCPU").get(0).getContext("2d");
	sysProcessMEM = $('canvas#processMEM').get(0).getContext("2d");
	sysActivity = $('canvas#activity').get(0).getContext("2d");
	reloadProcess();
	$.ajax({
		url : '/admin/activity/',
		type : 'GET',
		dataType : "json",
		complete : function(xhr, result) {
			json = JSON.parse(xhr.responseText); // parse de la réponse
			if (result == 'success' && json.registered == 'success') {
				var labels = [];
				var max = -100;
				for(var y = 0; y < json.results.length; ++y) { // parse des informations envoyées par le serveur
					labels.push(json.results[y].time.match(/[0-9]+-[0-9]+-[0-9]+/)[0]);
					max = (max >= parseInt(json.results[y].number)) ? max : parseInt(json.results[y].number);
				}
				var x;
				for (var y = 0; y < labels.length; ++y) {
					x = y + 1;
					while (x < labels.length) {
						if (labels[y] === labels[x]) {
							labels.splice(x, 1);
						} else {
							x++;
						}
					}
				}
				/* création des tableaux de valeur avec les informations envoyées par le serveur
				triées par date*/
				var datasets = {product_research : [], connection : [], registration : [], price_entry : []};
				for (var y = 0; y < json.results.length; ++y) {
					datasets[json.results[y].name].push(Math.round(json.results[y].number * 100 / max));
				}
				var date = {
					labels : labels,
					datasets : [
						{
							fillColor : "rgba(220,220,220,0.2)",
							strokeColor : "rgba(220,220,22, 1)",
							pointColor : "rgba(220,220,220,1)",
							pointStrokeColor : "#fff",
							data : datasets.product_research
						},
						{
							fillColor : "rgba(140,200,75,0.2)",
							strokeColor : "rgba(140,200,75,1)",
							pointColor : "rgba(140,200,75,1)",
							pointStrokeColor : "#fff",
							data : datasets.price_entry
						},
						{
							fillColor : "rgba(25,105,132,0.2)",
							strokeColor : "rgba(25,105,132,1)",
							pointColor : "rgba(25,105,132,1)",
							pointStrokeColor : "#fff",
							data : datasets.connection
						},
						{
							fillColor : "rgba(216,42,32,0.2)",
							strokeColor : "rgba(216,42,32,1)",
							pointColor : "rgba(216,42,32,1)",
							pointStrokeColor : "#fff",
							data : datasets.registration
						}
					]
				};
				new Chart(sysActivity).Line(date); // création du graphiques dans la balise canvas
			}
		}
	});
}

// permet de relancer toutes les 3 secondes la fonction reloadProcess afin d'avoir
// les informations sur les processus en quasi temps réel.
setInterval(reloadProcess, 3000);
