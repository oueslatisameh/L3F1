var map;
var ajaxRequest;
var plotlist;
var plotlayers=[];

function initmap() {
	map = new L.Map('map').setView([51.505, -0.09], 13);
	L.tileLayer('http://{s}.tile.cloudmade.com/8ee2a50541944fb9bcedded5165f09d9/997/256/{z}/{x}/{y}.png', {
    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://cloudmade.com">CloudMade</a>',
    maxZoom: 18
	}).addTo(map);
	var marker = L.marker([51.5, -0.09]).addTo(map);
}
