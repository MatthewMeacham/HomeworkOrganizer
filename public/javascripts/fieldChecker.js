$(document).ready(function() {
	$('#password').on('input', function(){
		checkPassword('password');
	});
	$('#name').on('input', function(){
		checkPassword('name');
	});
	$('#email').on('input', function(){
		checkPassword('email');
	});
	$('#subject').on('input', function(){
		checkPassword('subject');
	});
	$('#description').on('input', function(){
		checkPassword('description');
	});
});

function checkPassword(id) {
	var e = document.getElementById(id);
	if(e.value.length >= 250) {
		document.getElementById((id + 'Error')).innerHTML = "Too long";
	} else {
		document.getElementById((id + 'Error')).innerHTML = "";
	}
}