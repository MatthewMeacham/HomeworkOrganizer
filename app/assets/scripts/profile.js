$(document).ready(function() {
	$("#monthassignment").on('change', function() {
		changeDays();
	});
	$("#yearassignment").on('change', function() {
		changeDaysLeapYear();
	});
});

function removeOptions(selectbox) {
	var length = selectbox.options.length;
	for (i = length - 1; i >= 0; i--) {
		selectbox.remove(i);
	}
}

function changeDaysLeapYear() {
	if (document.getElementById('monthassignment').options[document
			.getElementById('monthassignment').selectedIndex].value == '02') {
		removeOptions(document.getElementById('daysassignment'));
		if (Number(document.getElementById('yearassignment').options[document
				.getElementById('yearassignment').selectedIndex].value) % 4 === 0) {
			for (i = 1; i < 30; i++) {
				var option = document.createElement("option");
				option.text = i;
				if (i < 10) {
					i = "0" + i;
				}
				option.value = i;
				document.getElementById('daysassignment').add(option);
			}
		} else {
			for (i = 1; i < 29; i++) {
				var option2 = document.createElement("option");
				option2.text = i;
				if (i < 10) {
					i = "0" + i;
				}
				option2.value = i;
				document.getElementById('daysassignment').add(option2);
			}
		}
	}
}

function changeDays() {
	var sel = document.getElementById('daysassignment');
	var selected = sel.options[sel.selectedIndex].value;
	var monthSelect = document.getElementById('monthassignment');
	var monthSelected = monthSelect.options[monthSelect.selectedIndex].value;
	removeOptions(sel);
	var opts = sel.options;
	switch (monthSelected) {
	case '01':
	case '03':
	case '05':
	case '07':
	case '08':
	case '10':
	case '12':
		for (i = 1; i < 32; i++) {
			var option = document.createElement("option");
			option.text = i;
			if (i < 10) {
				i = "0" + i;
			}
			option.value = i;
			sel.add(option);
		}
		break;
	case '04':
	case '06':
	case '09':
	case '11':
		for (i = 1; i < 31; i++) {
			var option2 = document.createElement("option");
			option2.text = i;
			if (i < 10) {
				i = "0" + i;
			}
			option2.value = i;
			sel.add(option2);
		}
		break;
	case '02':
		var years = document.getElementById('yearassignment');
		var yearOptions = years.options;
		if (Number(yearOptions[years.selectedIndex].text) % 4 === 0) {
			for (i = 1; i < 30; i++) {
				var option3 = document.createElement("option");
				option3.text = i;
				if (i < 10) {
					i = "0" + i;
				}
				option3.value = i;
				sel.add(option3);
			}
		} else {
			for (i = 1; i < 29; i++) {
				var option4 = document.createElement("option");
				option4.text = i;
				if (i < 10) {
					i = "0" + i;
				}
				option4.value = i;
				sel.add(option4);
			}
		}
		break;
	default:
		alert("NONE WERE CALLED");
	}
}

$(function() {
	var toggle = $('#toggle');
	menu = $('nav ul');
	menuHeight = menu.height();
	link = $('.clearfix a');
	pull = $('.pull');

	$(pull).on('click', function(e) {
		e.preventDefault();
		menu.slideToggle();
		e.stopPropagation();
	});
	$(menu).click(function(e) {
		var w = $(window).width();
		if (w <= 750) {
			$(menu).slideToggle();
			$('#toggle, .pull').toggleClass("on");
		}
	});

	$(window).resize(function() {
		var w = $(window).width();
		if (w > 320 && menu.is(':hidden')) {
			menu.removeAttr('style');
		}
	});
});

var submitted = false;

function submitAssignmentDeleteForm(id) {
	if (submitted)
		return;
	document.getElementById('assignmentDeleteForm' + id).submit();
}

function submitAssignmentEditForm(id) {
	if (submitted)
		return;
	document.getElementById('assignmentEditForm' + id).submit();
}

function submitFinishedAssignmentForm(id) {
	if (submitted)
		return;
	document.getElementById('assignmentFinishedForm' + id).submit();
}

function submitSchoolClassDeleteForm(id) {
	if (submitted)
		return;
	document.getElementById('schoolClassDeleteForm' + id).submit();
}

function submitSchoolClassEditForm(id) {
	if (submitted)
		return;
	document.getElementById('schoolClassEditForm' + id).submit();
}

function combine(id, category) {
	console.log("year" + category);
	var element = document.getElementById(id);
	var year = document.getElementById('year' + category).options[document
			.getElementById('year' + category).selectedIndex].value;
	var month = document.getElementById('month' + category).options[document
			.getElementById('month' + category).selectedIndex].value;
	var day = document.getElementById('days' + category).options[document
			.getElementById('days' + category).selectedIndex].value;
	console.log(year + "-" + month + "-" + day);
	element.value = year + "-" + month + "-" + day;
}

function toggle_visibility(id) {
	var e = document.getElementById(id);
	var anchor = document.getElementById(id + 'Anchor');
	document.getElementById('overview').style.display = 'none';
	document.getElementById('schoolClasses').style.display = 'none';
	document.getElementById('addAssignment').style.display = 'none';
	document.getElementById('lateAssignments').style.display = 'none';
	document.getElementById('finishedAssignments').style.display = 'none';
	document.getElementById('accountSettings').style.display = 'none';
	document.getElementById('errorMessageText').innerHTML = "";
	document.getElementById('overviewAnchor').style.color = "white";
	document.getElementById('schoolClassesAnchor').style.color = "white";
	document.getElementById('addAssignmentAnchor').style.color = "white";
	document.getElementById('lateAssignmentsAnchor').style.color = "white";
	document.getElementById('finishedAssignmentsAnchor').style.color = "white";
	document.getElementById('accountSettingsAnchor').style.color = "white";
	e.style.display = 'block';
	anchor.style.color = '#56ced6';
	e.style.animation = 'fadeIn .2s linear';
}

// Only called upon load
function changeNavColor(id) {
	var e = document.getElementById(id + 'Anchor');
	document.getElementById('overviewAnchor').style.color = "white";
	document.getElementById('schoolClassesAnchor').style.color = "white";
	document.getElementById('addAssignmentAnchor').style.color = "white";
	document.getElementById('lateAssignmentsAnchor').style.color = "white";
	document.getElementById('finishedAssignmentsAnchor').style.color = "white";
	document.getElementById('accountSettingsAnchor').style.color = "white";
	e.style.color = '#56ced6';
}

$(".pull").bind('click', function() {
	$('#toggle').toggleClass("on");
	return false;
});
