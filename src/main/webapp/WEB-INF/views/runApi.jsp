<%@ page language="java" contentType="text/html; charset=EUC-KR" pageEncoding="EUC-KR"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.ArrayList, java.text.DecimalFormat, ga.common.InformDao" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>Eduwill Ngene>��ǰ ����</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<link rel="stylesheet" href="/resources/css/style.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
</head>
<body id="myPage" data-spy="scroll" data-target=".navbar" data-offset="60">

	<!-- top nav -->
	<nav class="navbar navbar-inverse">
	<div class="container-fluid">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar">
				<span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="#">��ǰ ����</a>
		</div>
		<div class="collapse navbar-collapse" id="myNavbar">
			<ul class="nav navbar-nav">
				<li><a href="#Calendar">�Ⱓ ����</a></li>
				<li><a href="#Information">��ǰ ����</a></li>
				<li><a href="#Chart">���</a></li>
			</ul>
		</div>
	</div>
	</nav>
	
	<!-- main conatiner -->
	<div class="container-fluid text-center">
		<div class="row content">
		
			<!-- �Ⱓ ���� -->
			<div class="col-sm-12 text-left" id="Calendar">
				<h3 class="page-header">
					�Ⱓ ����<small> ������ / ������</small>
				</h3>
				<form action="/analytics" method="get">
					<input type="hidden" name="seq" value=${seq } />
					<div class="col-sm-5 text-left">
						������ : <input type="date" class="form-control" name="startDate" value=${param.startDate }>
					</div>
					<div class="col-sm-5 text-left">
						������ : <input type="date" class="form-control" name="endDate" value=${param.endDate }>
					</div>
					<div class="col-sm-2 text-center">
						<br /> <input type="submit" class="btn btn-primary btn-block" value="�˻�" />
					</div>
				</form>
			</div>
			
			<!-- ��ǰ ���� -->
			<div class="col-sm-12 text-left" id="Information">

				<h3 class="page-header">
					��ǰ ����<small> ��ġ Ȯ��</small>
				</h3>

				<div id="resultDisplay"></div>
				
				<c:choose>
					
					
					<c:when test="${result eq null}"> <!-- if(result == null) {...} -->
					<!-- �˻� ����� �������� ���� -->
						�˻� �Ⱓ�� ������ �ֽʽÿ�.<br/></div>
					</c:when>
					
					
					<c:otherwise> <!-- else {...} -->
					<!-- �˻� ����� ������ -->
						<table class="table table-hover">
							<thead>
								<tr>
									<th>URL</th>
									<th>��������</th>
									<th>�� ��������</th>
									<th>���� ��</th>
									<th>��Ż��</th>
									<th>��ȯ</th>
									<th>��ȯ��</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="inform" items="${result}" varStatus="status">
									<tr>
										<td>${inform.pagePath}</td>
										<td>${inform.pageviews}</td>
										<td>${inform.uniquePageviews}</td>
										<td>${inform.sessions}</td>
										<td>
											<script>
							    			var num = ${inform.bounceRate};
							    			document.write(num.toFixed(3));
							    			</script>
				    					</td>
										<td>${inform.totalEvents}</td>
										<td>
											<script>
							    			var num2 = ${inform.totalEvents*1.0 / inform.pageviews*1.0 * 100.0};
							    			document.write(num2.toFixed(3));
					    					</script>
					    				</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
					
					<!-- ���(��ƮȮ��) -->
					<div class="col-sm-12 text-left" id="Chart">
						<h3 class="page-header">
							���<small> ��Ʈ Ȯ��</small>
						</h3>
		
						<script>
						var mdate = ["1999-01-01"];
						var mpageView = [0];
						</script>
						
						<c:forEach var="pageView" items="${pageViews}" varStatus="status">
							<script>
								mdate.push("${pageView.mDate}");
								mpageView.push(${pageView.mPageView});
							</script>
						</c:forEach>
		
						<!-- CanvasJS Script -->
						<script type="text/javascript">
						window.onload = function () {
						
						var chart = new CanvasJS.Chart("chartContainer",
						{
							zoomEnabled: true,
								title:{
									text: "�Ϻ� �������� ��ȸ"
								},
								axisY:{
									includeZero: false
								},
								data: data,
							});
							chart.render();
						}
						
						var data = []; var dataSeries = { type: "line" };
						var dataPoints = [];
						for (var i = 1; i < mdate.length; i ++) {
							
							dataPoints.push({
								x: new Date(mdate[i]),
								y: mpageView[i]
							});
						}
						dataSeries.dataPoints = dataPoints;
						data.push(dataSeries);
						
						</script>
						
						<script src="/resources/js/canvasjs.min.js"></script>
						<div id="chartContainer" style="height: 400px; width: 100%;"></div>
					</div>
					</c:otherwise>				
				</c:choose>

		</div>
	</div>
	<script>
	$(document).ready(function(){
		
	  $(".navbar a, footer a[href='#myPage']").on('click', function(event) {
	    // Make sure this.hash has a value before overriding default behavior
	    if (this.hash !== "") {
	      // Prevent default anchor click behavior
	      event.preventDefault();
	
	      // Store hash
	      var hash = this.hash;
	
	      // Using jQuery's animate() method to add smooth page scroll
	      // The optional number (900) specifies the number of milliseconds it takes to scroll to the specified area
	      $('html, body').animate({
	        scrollTop: $(hash).offset().top
	      }, 900, function(){
	   
	        // Add hash (#) to URL when done scrolling (default click behavior)
	        window.location.hash = hash;
	      });
	    } // End if
	  });
	});
	</script>
</body>
</html>