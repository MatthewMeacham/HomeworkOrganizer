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
});

function checkPassword(id) {
	var e = document.getElementById(id);
	if(e.value.length >= 250) {
		document.getElementById((id + 'Error')).innerHTML = "Too long";
	} else {
		document.getElementById((id + 'Error')).innerHTML = "";
	}
}