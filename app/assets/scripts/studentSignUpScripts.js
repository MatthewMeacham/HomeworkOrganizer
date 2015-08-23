$(document).ready(function() {
	$('#password').on('input', function() {
		check('password');
		checkPassword('password');
	});
	$('#passwordAgain').on('input', function() {
		check('passwordAgain');
		comparePasswords();
		checkPassword('passwordAgain');
	});
	$('#name').on('input', function() {
		check('name');
	});
	$('#email').on('input', function() {
		check('email');
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

function comparePasswords() {
	var password = document.getElementById('password').value;
	var passwordAgain = document.getElementById('passwordAgain').value;
	if (password != passwordAgain) {
		document.getElementById('passwordAgainError').innerHTML = "Passwords do not match.";
	}
}

function checkPassword(id) {
	var password = document.getElementById(id).value;
	if (password.length < 8) {
		document.getElementById(id + 'Error').innerHTML = "Password must be at least 8 chracters.";
	}
}
