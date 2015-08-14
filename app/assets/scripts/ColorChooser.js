/* TOGGLE THE COLOR PALETTE ON ICON CLICK AND CLOSE THE COLOR PALETTE WHEN USER CLICKS AWAY */
$(document).ready(
		function() {
			$(".logo").click(function(event) {
				event.stopPropagation();
				$(".colors").slideToggle(300);
				window.scrollTo(0, document.body.scrollHeight);
			});

			$(document).click(function() {
				$('.colors').hide();
			});

			/* COLORS ALONG WITH THEIR HEX CODES STORED IN AN ARRAY */
			var ids = [ '.redForColorChooser', '.orange',
					'.yellowForColorChooser', '.greenForColorChooser', '.blue',
					'.indigo', '.purple', '.pink', '.whiteForColorChooser',
					'.black', '.grey', '.brown', '.reset' ];
			var colors = [ '#C66761', '#FFB550', '#FDE74C', '#74CD79',
					'#61C0C6', '#3A9AA1', '#C661C0', '#FF7CEB', '#FFF',
					'#454545', '#BFBFBF', '#C69A61', '#C54340' ];

			/*
			 * LOOP THROUGH THE COLORS IN THE ARRAYS AND SUCCESSFULLY PLACE
			 * COLOR TO CLASS ON USER CLICK
			 */
			$.each(ids, function(index, value) {
				$(value).click(function() {
					$(".logo").css('background-color', colors[index]);
					if (index == 8 || index == 2) {
						$(".colorText").css('color', 'black');
					} else {
						$(".colorText").css('color', 'white');
					}
				});
			});
		});

function changeColorField(color) {
	var colors = [ '#C66761', ' #FFB550', '#FDE74C', '#74CD79', '#61C0C6',
			'#3A9AA1', '#C661C0', '#FF7CEB', '#454545', '#BFBFBF', '#FFF',
			'#C69A61', ' #C54340' ];
	var e = document.getElementById('color');

	switch (color) {
	case 'red':
		e.value = colors[0];
		break;
	case 'orange':
		e.value = colors[1];
		break;
	case 'yellow':
		e.value = colors[2];
		break;
	case 'green':
		e.value = colors[3];
		break;
	case 'blue':
		e.value = colors[4];
		break;
	case 'indigo':
		e.value = colors[5];
		break;
	case 'purple':
		e.value = colors[6];
		break;
	case 'pink':
		e.value = colors[7];
		break;
	case 'black':
		e.value = colors[8];
		break;
	case 'gray':
		e.value = colors[9];
		break;
	case 'white':
		e.value = colors[10];
		break;
	case 'brown':
		e.value = colors[11];
		break;
	case 'default':
		e.value = "#C54340";
		break;
	default:
		e.value = "#C54340";
	}
}

function changeColorFieldByHex(hex) {
	var e = document.getElementById('color');
	e.value = hex;
	var logo = document.getElementById('logo');
	logo.style.background = hex;
	if (hex == 'FFE629' || hex == 'FF61E7') {
		document.getElementById("colorText").style.color = '1F1F1F';
	} else {
		document.getElementById("colorText").style.color = 'FFF';
	}
}
