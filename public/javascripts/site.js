// const DEFAULT_TXS_DETAILS_VM = {
// 	"id": "",
// 	"plmId": "1234567",
// 	"captureSet": "",
// 	"runFolder": "",
// 	"testId": "",
// 	"libId": "",
// 	"accId": "",
// 	"recordId": "CVT-",
// 	"referenceId": "",
// 	"panelName": "",
// };

// const txsDetailsVM = ko.mapping.fromJS(DEFAULT_TXS_DETAILS_VM, document.getElementById('txsDetailsModal'));
const EnrollmentService = {
	onFetchTrxDetailsSuccess: function(response){
		if(!response || typeof response !== 'object'){
			console.error('onFetchTrxDetailsSuccess: Error: Invalid parameter "response" => ' + JSON.stringify(response, null, 2));
			return;
		}
		if(response.result !== 'SUCCESS' || !response.keyValueTable || !Array.isArray(response.keyValueTable)){
			//TODO show modal
			return;
		}

		const data = response.keyValueTable[0];
		ko.mapping.fromJS(data, EnrollmentService.tsxViewModel);
		$('#txsDetailsModal').modal('show');
	},
	onFetchTrxDetailsFailure: function(xhr, error){
		if(xhr){
			console.debug(xhr);
		}
		if(error){
			console.error('onFetchTrxDetailsFailure: Error: ' + error);
			showErrorModal(message);
			return;
		}
	},
	showErrorModal: function(message){
		$('#errorMessage').text(message);
		$('#errorModal').modal('show');
	}
};

$('.idBtn').click(function(event)
{
	const transactionId = event.target.innerText;
	if(!transactionId){
		console.error('idBtn.click: Error: Invalid transactionId!');
		return;
	}

	const requestSettings = {
		url: '/' + transactionId,
		method: 'GET',
		accepts: {
			mycustomtype: 'application/json'
	  	},
	  	statusCode:{
	  		400: function(){EnrollmentService.showErrorModal('Could not retrive transaction details.');},
	  		500: function(){EnrollmentService.showErrorModal('Something went wrong processing the request.');}
	  	}
	};

	$.ajax(requestSettings).done(EnrollmentService.onFetchTrxDetailsSuccess).fail(EnrollmentService.onFetchTrxDetailsFailure);
});

$(document).ready(function() {
	const DEFAULT_TXS_DETAILS_VM = {
		"id": "",
		"plmId": "",
		"captureSet": "",
		"runFolder": "",
		"testId": "",
		"libId": "",
		"accId": "",
		"recordId": "CVT-",
		"token": "",
		"panelName": "",
		"log": ""
	};

	$('#table').DataTable();
	EnrollmentService.tsxViewModel = ko.mapping.fromJS(DEFAULT_TXS_DETAILS_VM, document.getElementById('txsDetailsModal'));
	ko.applyBindings(EnrollmentService.tsxViewModel);
} );
