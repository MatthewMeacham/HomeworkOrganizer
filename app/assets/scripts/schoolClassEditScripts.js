function changeColorField(color) {
	var colors = ['#ED2828', '#FA9F20', '#FFE629', '#61FA85', '#7CD7E6', '#0060FA', '#941FCF', '#FF61E7', '#1F1F1F', '#737373', '#FFF', '#8F5E3B', ' #c4413b'];
	var e = document.getElementById('color');
	
	switch(color){
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
	default:
		e.value = "#c34444";
	}
}

function submitSchoolClassEdit() {
	
}
