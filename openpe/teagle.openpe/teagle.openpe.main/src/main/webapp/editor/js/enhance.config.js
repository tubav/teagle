var screenMedia = enhance.mediaquery('screen and (max-device-width: 1023px)') ? 'screen and (device-min-width: 5000px)' : 'screen',
	handheldMedia = 'screen and (max-device-width: 1023px)';

enhance({
	loadStyles: [
		{media: screenMedia, href: 'css/screen.css'},
		{media: screenMedia, href: 'http://fonts.googleapis.com/css?family=Droid+Sans|Droid+Sans+Mono'},
		{media: handheldMedia, href: 'css/handheld.css'},
		{media: 'print', href: 'css/print.css'},
		{media: screenMedia, href: 'css/msie.css', iecondition: 'lt 9'}
	],
	loadScripts: [
		{excludemedia: 'print', src: 'js/jquery.js'},
		{excludemedia: 'print', src: 'js/jquery.history.js'},
		{excludemedia: 'print', src: 'js/jquery.expandable.js'},
		{excludemedia: 'print', src: 'js/jquery.easing.js'},
		{excludemedia: 'print', src: 'js/jquery.ui.js'},
		{excludemedia: 'print', src: 'js/common.js'},
		{excludemedia: 'print', src: 'js/jquery.validate.js'},
		{media: screenMedia, src: 'js/screen.js'},
		{media: handheldMedia, src: 'js/handheld.js'},
		{src: 'js/msie_html5.js', iecondition: 'lt 9'}
	],
	forcePassText: 'Enhanced version',
	forceFailText: 'Basic version' 	
}); 