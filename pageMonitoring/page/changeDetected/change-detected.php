<?PHP
require_once("./../../include/membersite_config.php");

if(!$fgmembersite->checkLogin()) {
    $fgmembersite->redirectToURL("./../login/login.php");
    exit;
}

if(isset($_POST['submitted'])) {
	if($fgmembersite->checkChanges()) {
		$fgmembersite->redirectToURL("change-detected.php");
	}
}

$changesDetected = $fgmembersite->getChangesDetected();
$display_block = '';
if (!empty($changesDetected)) {
	foreach ($changesDetected as $value) {
		$display_block .= "
		<tr>
			<td>".$value['id']."</td>
			<td>".$value['dateChangeDetected']."</td>
			<td><button type='button' onclick='showHtmlDetailPopup(".$value['id'].")'>see</button></a></td>
		</tr>";
	}
}

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
      <meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
      <title>Change Detected</title>
      <link rel="STYLESHEET" type="text/css" href="./../../style/fg_membersite.css">      
	  <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.2/themes/smoothness/jquery-ui.css"/>
	  <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
	  <script src="http://code.jquery.com/ui/1.10.2/jquery-ui.js"></script>
	  <script>
		  function showHtmlDetailPopup(changeDetectedId) {
		  	if (changeDetectedId == "") {
		    	document.getElementById("htmlDetailPopup").innerHTML = "";
		    	return;
		    } 
		    
		  	if (window.XMLHttpRequest) {
			  	// code for IE7+, Firefox, Chrome, Opera, Safari
		    	xmlhttp = new XMLHttpRequest();
		    } else {
			    // code for IE6, IE5
		    	xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
		    }
		    
		  	xmlhttp.onreadystatechange = function() {
			  	if (xmlhttp.readyState==4 && xmlhttp.status==200) {
				  	document.getElementById("htmlDetailPopup").innerHTML = xmlhttp.responseText;
				  	$("#htmlDetailPopup").dialog({height: 700, width: 1000, modal: true});
				}
		    };

		  	xmlhttp.open("GET", "getChangeDetectedHtml.php?changeDetectedId=" + changeDetectedId, true);
			xmlhttp.send();
	    }		  
	  </script>
</head>
<body>
<div id='fg_membersite'>
	<h2>Change Detected</h2>
	This page shows all the changes detected on the page monitored.
	<p>
	Logged in as: <?= $fgmembersite->UserFullName() ?>
	</p>

	<div id="htmlDetailPopup" style="display: none; overflow: hidden;" title="Html Detail">
	</div>
	
	<!-- Form Code Start -->
	<form id='registerChangeDetected' action='<?php echo $fgmembersite->GetSelfScript(); ?>' method='post' accept-charset='UTF-8'>
		<fieldset style="width: 90%;">
			<legend>Change Detected Log</legend>
			
			<input type='hidden' name='submitted' id='submitted' value='1'/>
			
			<input type='text' class='spmhidip' name='<?php echo $fgmembersite->GetSpamTrapInputName(); ?>' />
			
			<div><span class='error'><?php echo $fgmembersite->GetErrorMessage(); ?></span></div>
			<div class='container'>
			    <input type='submit' name='Check' value='Check' />
			</div>
			<table border="1" align="center">
				<tr>
					<td><strong>ID</strong></td>
					<td><strong>DATE CHANGE DETECTED</strong></td>
					<td><strong>HTML</strong></td>
				</tr>
				<? //open a php block to populate the table
					echo $display_block;
				   //close the php block and then the table
				?>
			</table>
		</fieldset>
	</form>
	<!--
	Form Code End (see html-form-guide.com for more info.)
	-->
	
	<p>
	<a href='./../login/login-home.php'>Home</a>
	</p>
</div>
</body>
</html>