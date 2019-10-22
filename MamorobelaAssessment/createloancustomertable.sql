CREATE TABLE `loancustomer` (
  `idloanCustomer` int(11) NOT NULL AUTO_INCREMENT,
  `accountNumber` int(11) DEFAULT NULL,
  `receiveRate` double DEFAULT NULL,
  `receiveYears` double DEFAULT NULL,
  `receiveAmt` double DEFAULT NULL,
  `totalAmount` double DEFAULT NULL,
  `monthlyPayment` double DEFAULT NULL,
  `firstName` varchar(45) DEFAULT NULL,
  `lastName` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idloanCustomer`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
