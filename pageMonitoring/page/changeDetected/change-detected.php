<?PHP
require_once("./../../include/membersite_config.php");

if(!$fgmembersite->checkLogin()) {
    $fgmembersite->redirectToURL("./../login/login.php");
    exit;
}

if(isset($_POST['submitted'])) {
	if($fgmembersite->registerPageMonitored()) {
		$fgmembersite->redirectToURL("change-detected.php");
	}
}

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
      <meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
      <title>Page Monitored</title>
      <link rel="STYLESHEET" type="text/css" href="./../../style/fg_membersite.css">
</head>
<body>
<div id='fg_membersite'>
	<h2>Page Monitored</h2>
	This page can change the monitored page and trigger the process to detect changes at the moment or change the time to check automatically.
	<p>
	Logged in as: <?= $fgmembersite->UserFullName() ?>
	</p>

	<!-- Form Code Start -->
	<form id='registerPageMonitored' action='<?php echo $fgmembersite->GetSelfScript(); ?>' method='post' accept-charset='UTF-8'>
		<fieldset>
			<legend>Register Page Monitored</legend>
			
			<input type='hidden' name='submitted' id='submitted' value='1'/>
			
			<div class='short_explanation'>* required fields</div>
			<input type='text' class='spmhidip' name='<?php echo $fgmembersite->GetSpamTrapInputName(); ?>' />
			
			<div><span class='error'><?php echo $fgmembersite->GetErrorMessage(); ?></span></div>
			<div class='container'>
			    <label for='monitoredUrl'>Monitored URL*: </label><br/>
			    <input type='text' name='monitoredUrl' id='monitoredUrl' value='<?php echo $fgmembersite->SafeDisplay('monitoredUrl') ?>' maxlength="256"/><br/>
			    <span id='register_monitoredUrl_errorloc' class='error'></span>
			</div>
			<div class='container'>
			    <label for='checkFrequencyMin'>Check Frequency (Minutes)*:</label><br/>
			    <input type='text' name='checkFrequencyMin' id='checkFrequencyMin' value='<?php echo $fgmembersite->SafeDisplay('checkFrequencyMin') ?>' maxlength="50"/><br/>
			    <span id='register_checkFrequencyMin_errorloc' class='error'></span>
			</div>
			<div class='container'>
			    <label for='lastCheckDone'>Last Check Done:</label><br/>
			    <input type='text' disabled="disabled" name='lastCheckDone' id='lastCheckDone' value='<?php echo $fgmembersite->SafeDisplay('lastCheckDone') ?>' maxlength="50"/><br/>
			</div>
			<div class='container'>
			    <label for='lastChangeDetected' >Last Change Detected:</label><br/>
			    <input type='text' disabled="disabled" name='lastChangeDetected' id='lastChangeDetected' value='<?php echo $fgmembersite->SafeDisplay('lastChangeDetected') ?>' maxlength="50"/><br/>
			</div>
			
			<div class='container'>
			    <input type='submit' name='Save' value='Save & Check' />
			</div>
		</fieldset>
	</form>
	<!--
	Form Code End (see html-form-guide.com for more info.)
	-->

	<!-- client-side Form Validations:
	Uses the excellent form validation script from JavaScript-coder.com-->
	<script type='text/javascript'>
	// <![CDATA[
	    var frmvalidator  = new Validator("registerPageMonitored");
	    frmvalidator.EnableOnPageErrorDisplay();
	    frmvalidator.EnableMsgsTogether();
	    frmvalidator.addValidation("monitoredUrl","req","Please provide your monitoredUrl");
	    frmvalidator.addValidation("checkFrequencyMin","req","Please provide checkFrequencyMin");
	// ]]>
	</script>

	<p>
	<a href='./../login/login-home.php'>Home</a>
	</p>
</div>
</body>
</html>