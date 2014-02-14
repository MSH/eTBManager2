/*
 * Update the time zone of Bangladesh server from the USA (UTC-06:00) to Bangladesh (UTC+6:00),
 * i.e, 12 hours difference
 */


update casecomment set comment_date = comment_date + interval 8 hour;
update errorlog set errorDate = errorDate + interval 8 hour;
update forecasting set recordingDate = recordingDate + interval 8 hour;
update issue set creationdate = creationdate + interval 8 hour,
  answerdate = answerdate + interval 8 hour;
update issuefollowup set followupdate = followupdate + interval 8 hour;
update movement set recorddate = recorddate - interval 8 hour;
update medicineorder set orderdate = orderdate + interval 8 hour,
  approvingdate = approvingdate + interval 8 hour;
update ordercomment set date = date + interval 8 hour;
update transactionlog set transactiondate = transactiondate + interval 8 hour;
update sys_user set registrationdate = registrationdate + interval 8 hour;
update userlogin set logindate = logindate + interval 8 hour;
update userlogin set logoutdate = logoutdate + interval 8 hour;

