$(document).ready(function() {
	$('#password').on('input', function() {
		check('password');
		checkPasswordLength();
	});
	$('#passwordAgain').on('input', function() {
		check('passwordAgain');
		checkPasswords();
		checkPasswordLength();
	});
	$('#name').on('input', function() {
		check('name');
	});
	$('#email').on('input', function() {
		check('email');
	});
	$('#subject').on('input', function() {
		check('subject');
	});
	$('#description').on('input', function() {
		check('description');
	});
});

function check(id) {
	var e = document.getElementById(id);
	if (e.value.length >= 250) {
		document.getElementById((id + 'Error')).innerHTML = "Too long";
	} else {
		document.getElementById((id + 'Error')).innerHTML = "";
	}
}

function checkPasswords() {
	var password = document.getElementById('password').value;
	var passwordAgain = document.getElementById('passwordAgain').value;
	if (password != passwordAgain) {
		document.getElementById('passwordAgainError').innerHTML = "Passwords do not match.";
	}
}

function checkPasswordLength(id) {
	var password = document.getElementById(id).value;
	if (value.length < 8) {
		document.getElementById(id + 'Error').innerHTML = "Password must be at least 8 chracters.";
	}
}
