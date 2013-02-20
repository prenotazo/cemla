<?php

require 'MainClass.php';

class SecondClass {
	
	private $myVar;
	
	public function __construct($var = '') {
		$this->myVar = new MainClass();
	}
}