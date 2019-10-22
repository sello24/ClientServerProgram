CREATE TABLE `userrequests` (
  `iduserRequestsID` int(11) NOT NULL AUTO_INCREMENT,
  `accountNumber` int(11) DEFAULT NULL,
  `requestedAmount` double DEFAULT NULL,
  `rate` decimal(10,0) DEFAULT NULL,
  `numberOfYears` decimal(10,0) DEFAULT NULL,
  `monthlyPayment` double DEFAULT NULL,
  `dateRequested` date DEFAULT NULL,
  `firstName` varchar(45) DEFAULT NULL,
  `lastName` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`iduserRequestsID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
