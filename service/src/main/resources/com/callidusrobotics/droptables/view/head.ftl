<title>DropTable Reports</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta charset="utf-8">

<!-- Allow IE6-8 support of HTML5 elements (must be in head section) -->
<!--[if lt IE 9]>
<script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->

<style>
      table {
        width: 100%;
      }

      table, th, td {
        border: 1px solid black;
        border-collapse: collapse;
      }      
</style>

<link rel="stylesheet" href="./css/style.css">
<script src="http://ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script>

<!-- <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script> -->

<script>
    $(document).ready(
  /* This is the function that will get executed after the DOM is fully loaded */
  function () {
    /* Next part of code handles hovering effect and submenu appearing */
    $('.nav li').hover(
      function () { //appearing on hover
        $('ul', this).fadeIn();
      },
      function () { //disappearing on hover
        $('ul', this).fadeOut();
      }
    );
  }
);
</script>