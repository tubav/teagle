function appendToggleLinks(){
	if(enhance.cookiesSupported && !$('.mobiledesktoptoggle').length && $('.enhanced_toggleResult').length){
		$('<a href="#" class="mobiledesktoptoggle">Mobile version</a>')
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

	$('#nav-left li:not(.current) ul').css({display:'none'});
	$('#nav-left > ul > li:not(.current) > a').toggle(function(){
		$(this).next('ul').css({display:'block'});
	},function(){
		$(this).next('ul').css({display:'none'});
	});


	$('textarea').expandable();


	initTabs();
	initWizzards();
	initAccordions();
	initDialogs();
	initAutocomplete();

	$('.wizzard nav a').click(function(){
		var $tabs = $(this).parents('.wizzard');
		var selected = $tabs.tabs('option','selected');
		//var number = $tabs.tabs('length');
		var next = selected+1;
		var back = selected-1;
		var finish = selected+1;
		var enabled = isEnabled();
		var state = $(this).attr('class');

		switch(state){
			case "next":
				if(enabled==1){
					$tabs.tabs('enable',next).tabs('select',next);
				}
			break;
			case "back":
				$tabs.tabs('select',back).tabs('disable',next);
			break;
			case "finish":
				if(enabled==1){
					$tabs.tabs('enable',next).tabs('select',next);
				}
			break;
		}
		
		return false;
	});

	appendToggleLinks();
});