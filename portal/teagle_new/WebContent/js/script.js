function toggledisplay (id){
  if (document.getElementById) {
    var mydiv = document.getElementById(id);
    mydiv.style.display = (mydiv.style.display=='block'?'none':'block');
  }
}