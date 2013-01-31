/*! Copyright (c) 2010 Brandon Aaron (http://brandonaaron.net)
 * Licensed under the MIT License (LICENSE.txt).
 *
 * Version 1.0
 *
 * Contributions by:
 *   - Karl Swedberg
 */

(function($){$.fn.extend({expandable:function(givenOptions){var options=$.extend({duration:'normal',interval:750,within:1,by:2,init:false},givenOptions);return this.filter('textarea').each(function(){var $this=$(this).css({display:'block',overflow:'hidden'}),minHeight=$this.height(),heightDiff=this.offsetHeight-minHeight,rowSize=(parseInt($this.css('lineHeight'),10)||parseInt($this.css('fontSize'),10)),$mirror=$('<div style="position:absolute;top:-999px;left:-999px;border-color:#000;border-style:solid;overflow-x:hidden;visibility:hidden;z-index:0;white-space: pre-wrap;white-space:-moz-pre-wrap;white-space:-pre-wrap;white-space:-o-pre-wrap;word-wrap:break-word;" />').appendTo('body'),interval;$.each('borderTopWidth borderRightWidth borderBottomWidth borderLeftWidth paddingTop paddingRight paddingBottom paddingLeft fontSize fontFamily fontWeight fontStyle fontStretch fontVariant wordSpacing lineHeight width'.split(' '),function(i,prop){$mirror.css(prop,$this.css(prop));});$this.bind('keypress',function(event){if(event.keyCode=='13')check();}).bind('focus blur',function(event){if(event.type=='blur')clearInterval(interval);if(event.type=='focus')interval=setInterval(check,options.interval);});function check(){var text=$this.val(),newHeight,height,usedHeight,usedRows,availableRows;$mirror.html(encodeHTML(text).replace(/\n/g,'&nbsp;<br>'));height=$this[0].offsetHeight-heightDiff;usedHeight=$mirror[0].offsetHeight-heightDiff;usedRows=Math.floor(usedHeight/rowSize);availableRows=Math.floor((height/rowSize)-usedRows);if(availableRows<=options.within){newHeight=rowSize*(usedRows+Math.max(availableRows,0)+options.by);$this.stop().animate({height:newHeight},options.duration);}else if(availableRows>options.by+options.within){newHeight=Math.max(height-(rowSize*(availableRows-(options.by+options.within))),minHeight);$this.stop().animate({height:newHeight},options.duration);}};if(options.init)check();}).end();}});function encodeHTML(text){var characters={'<':'&lt;','>':'&gt;','&':'&amp;','"':'&quot;','\'':'&#x27;','/':'&#x2F;'};return(text+'').replace(/[<>&"'\/]/g,function(c){return characters[c];});}})(jQuery);