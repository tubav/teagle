$('<meta name="viewport" content="width=device-width,initial-scale=1"/><meta name="apple-mobile-web-app-capable" content="yes"/><meta name="apple-mobile-web-app-status-bar-style" content="black"/><link rel="apple-touch-icon" href="img/touchicon.png"/><link rel="apple-touch-startup-image" href="img/startup.png"/>').appendTo('head');



function appendToggleLinks(){
	if(enhance.cookiesSupported && !$('.mobiledesktoptoggle').length && $('.enhanced_toggleResult').length){
		$('<a href="#" class="mobiledesktoptoggle">Desktop version</a>')
			.click(function(){
				enhance.toggleMedia(screenMedia, handheldMedia);
				return false;
			})
			.appendTo('body');
			
		$('.mobiledesktoptoggle, .enhanced_toggleResult').wrapAll('<nav id="toggleVersion"></nav>');	

		$('.enhanced_toggleResult').css('opacity','1');
		$('#toggleVersion').delay(500).animate({opacity:'1'},333,'easeOutCubic');
	}
	else{
		setTimeout(appendToggleLinks,50);
	}
}


$(function(){




	$('textarea').expandable();

	appendToggleLinks();

	setTimeout(scrollTo,0,0,1);
});