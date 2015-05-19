var Gmap = Gmap || {};

var caseTmp = {latLng: [0.0, 0.0], id: "case"};
var volTmp = {latLng: [0.0, 0.0], id: "vol",
	options: {icon: "http://www.veryicon.com/icon/png/System/Farm%20Fresh/bullet%20black.png"}
};

Gmap = function(element) {
	this.element = element;
	this.element.innerHTML = "<div id='map-canvas'></div>";

	this.init = function() {
		var script = document.createElement('script');
		script.type = 'text/javascript';
		script.src = "http://maps.google.com/maps/api/js?libraries=places&sensor=true&"
				+ "callback=initialize";
		document.body.appendChild(script);
	};

	this.popup = function(value) {
		alert(value);
	};

	this.updateCase = function(caseData) {
		var caseDataObj = jQuery.parseJSON(caseData);
		console.log("Update Case:");
		console.log(caseDataObj);

		var caseAddress = caseDataObj.caseAddress;
		var caseLocation = caseDataObj.caseLocation;
		var caseLatLng = new google.maps.LatLng(caseLocation.latitude, caseLocation.longitude);
		var caseVolunteers = caseDataObj.volunteers;
		console.log("Volunteerslist:");
		console.log(caseVolunteers);

		//reorganizing marker values
		var values = new Array();

		var caseMarker = jQuery.extend({}, caseTmp);  //var caseMarker = caseTmp; creates pointer... not very good...
		caseMarker.latLng = new Array(caseLocation.latitude, caseLocation.longitude);

		values.push(caseMarker);

		$.each(
				caseVolunteers,
				function(i, volunteer) {
					console.log("Voltmp before:");
					console.log(volTmp);

					var marker = jQuery.extend({}, volTmp);  //var marker = volTmp; creates pointer... not very good...
					marker.latLng = new Array(volunteer.location.latitude, volunteer.location.longitude);
					marker.id = volunteer.email;

					console.log("Voltmp after:");
					console.log(volTmp);

					console.log("Marker added:");
					console.log(marker);

					values.push(marker);
				}
		);

		console.log("Array values:");
		console.log(values);

		/*
		 $(caseVolunteers).each(function( i ){            
		 var valueTmp = volTmp;
		 valueTmp.id = this.email;
		 valueTmp.latLng = new Array();
		 valueTmp.latLng = [this.location.latitude, this.location.longitude];
		 values[i + 1] = valueTmp;
		 });
		 console.log(values);
		 */

		updateMaps(caseLatLng, values);
		setPlace(caseLatLng);
	};

};

window.initialize = function() {
	//setup autocomplete interface on textarea
	var input = document.getElementById('address');
	var autocomplete = new google.maps.places.Autocomplete(input, {});

	google.maps.event.addListener(autocomplete, 'place_changed', function() {
		setMaps(autocomplete.getPlace().geometry.location);
	});

	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(initMaps, failedMaps);
	} else {
		alert("Browser does not support Geolocation. Creating pseudo-location (Aachen)");
	}

};

//on failed browser-location create a dummy and log error
function failedMaps(error) {
	setMaps(new google.maps.LatLng(50.776585, 6.083612));
}

function initMaps(position) {
	setMaps(new google.maps.LatLng(position.coords.latitude, position.coords.longitude));
}

function updateMaps(position, values) {
	$("#map-canvas").height("100%").width("100%").gmap3({
		clear: {
			name: ["marker"]
		},
		map: {
			options: {
				center: [position.lat(), position.lng()],
				zoom: 15,
				mapTypeId: google.maps.MapTypeId.ROADMAP
			}
		},
		marker: {
			values: values,
			options: {
				draggable: true
			},
			events: {
				mouseup: function(marker, event) {
					console.log(marker);
					$(this).gmap3({panTo: marker.getPosition()});
					setPlace(marker.getPosition());
				}
			}
		},
		events: {
			center_changed: function(map, event) {
				$(this).gmap3().panTo(position);
			}
		}
	});
}

function setMaps(position) {
	var currentPosition = position;

	$("#map-canvas").height("100%").width("100%").gmap3({
		clear: {
			name: ["marker"]
		},
		map: {
			options: {
				center: [currentPosition.lat(), currentPosition.lng()],
				zoom: 15,
				mapTypeId: google.maps.MapTypeId.ROADMAP
			}
		},
		marker: {
			values: [
				{
					latLng: [currentPosition.lat(), currentPosition.lng()],
					data: "Marker"
				}
			],
			options: {
				draggable: true
			},
			events: {
				mouseup: function(marker, event) {
					setPlace(marker.getPosition());
				}
			}
		}
	});

	//set before drag
	$('#latitude').attr('value', currentPosition.lat());
	$('#longitude').attr('value', currentPosition.lng());

	refreshUI();
}

//sets address determined by marker
function setPlace(location) {
	if (typeof geocoder === "undefined") {
		geocoder = new google.maps.Geocoder();
	}

	geocoder.geocode({'latLng': location}, function(results, status) {
		if (status == google.maps.GeocoderStatus.OK) {
			if (results[1]) {
				$('#address').val(results[0].formatted_address).focus().blur();
				refreshUI();
			}
		} else {
			alert("Geocoder failed due to: " + status);
		}
	});

	$('#latitude').attr('value', location.lat());
	$('#longitude').attr('value', location.lng());
	refreshUI();
}

function refreshUI() {
	$('#latitude').focus().blur();
	$('#longitude').focus().blur();
	$('#address').focus().blur();
	$('#notes').focus().blur();
	$('#init-id').focus().blur();
}
