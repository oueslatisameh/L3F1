//<table border="1">
//	<tr>
//		<th>routes ajoutées dans</th> <th><a href="app.html"><b>app.js</b></a></th>
//	</tr>
//</table>

//Route GET <b>/about</b><br />
// <small>route renvoyant la page du projet</small>  
//<ul>
//	<li>sortie</li>
//	<ul>
//		<li><b>Page HTML</b></li>
//	</ul>
//</ul>
exports.getAbout = function(config) {
	return (function(req, res){
		res.render('about', {web : config.web, co : true});
	});
};