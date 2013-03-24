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

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
      <meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
      <title>Change Detected</title>
      <link rel="STYLESHEET" type="text/css" href="./../../style/fg_membersite.css">
</head>
<body>
<div id='fg_membersite'>
	<h2>Change Detected</h2>
	This page shows all the changes detected on the page monitored.
	<p>
	Logged in as: <?= $fgmembersite->UserFullName() ?>
	</p>

	<!-- Form Code Start -->
	<form id='registerChangeDetected' action='<?php echo $fgmembersite->GetSelfScript(); ?>' method='post' accept-charset='UTF-8'>
		<fieldset>
			<legend>Change Detected Log</legend>
			
			<input type='hidden' name='submitted' id='submitted' value='1'/>
			
			<input type='text' class='spmhidip' name='<?php echo $fgmembersite->GetSpamTrapInputName(); ?>' />
			
			<div><span class='error'><?php echo $fgmembersite->GetErrorMessage(); ?></span></div>
			<div class='container'>
			    <input type='submit' name='Check' value='Check' />
			</div>

			<div class='container'>
			    <label for='lastChangeDetected' >Last Change Detected:</label><br/>
			    <input type='text' disabled="disabled" name='lastChangeDetected' id='lastChangeDetected' value='<?php echo $fgmembersite->safeDisplay('lastChangeDetected') ?>' maxlength="50"/><br/>
			</div>
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