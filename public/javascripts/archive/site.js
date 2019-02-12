var ArchiveQueueService = {
    onFetchTrxDetailsSuccess: function(response) {
        if (!response || typeof response !== 'object') {
            console.error('onFetchTrxDetailsSuccess: Error: Invalid parameter "response" => ' + JSON.stringify(response, null, 2));
            return;
        }
        if (response.result !== 'SUCCESS' || !response.keyValueTable || !Array.isArray(response.keyValueTable)) {
            const props = response.props;
            const plmID = response.plmID;

            const requestSettings = {
                url: '/tool/archive/' + plmID,
                method: 'GET',
                accepts: {
                    mycustomtype: 'application/json'
                },
                statusCode: {
                    400: function() {
                        ArchiveQueueService.showErrorModal('Could not retrive plm transaction.');
                    },
                    500: function() {
                        ArchiveQueueService.showErrorModal('Something went wrong processing the request.');
                    }
                }
            };

            $.ajax(requestSettings).done(function(resposne) {
                console.log(resposne);
            }).fail(function(e) {
                console.log(e);
            });
            return;
        }
    },
    onFetchTrxDetailsFailure: function(xhr, error) {
        if (xhr) {
            console.debug(xhr);
        }
        if (error) {
            console.error('onFetchTrxDetailsFailure: Error: ' + error);
            showErrorModal(message);
            return;
        }
    },
    showErrorModal: function(message) {
        $('#errorMessage').text(message);
        $('#errorModal').modal('show');
    },
    showConfirmationModal: function(message) {
        $('#confirmationModal').modal('show');
    }

};

$('#confirmationModal').on('show.bs.modal', function(e) {
    ArchiveQueueService.pid = e.relatedTarget.dataset.yourparameter;
})

$('#invalidModal').on('show.bs.modal', function(e) {
    alert("" + window.location.hostname + ":9001/forceArchive/" + e.relatedTarget.dataset.yourparameter);
})

$('#confirmBtn').on('click', function(e) {
    var transactionId = ArchiveQueueService.pid;
    $('#confirmationModal').modal('toggle');

    if (!transactionId) {
        console.error('idBtn.click: Error: Invalid transactionId!');
        return;
    }

    const requestSettings = {
        url: '/plm/plmProp/' + transactionId,
        method: 'GET',
        accepts: {
            mycustomtype: 'application/json'
        },
        statusCode: {
            400: function() {
                ArchiveQueueService.showErrorModal('Could not retrive plm transaction.');
            },
            500: function() {
                ArchiveQueueService.showErrorModal('Something went wrong processing the request.');
            }
        }
    };

    $.ajax(requestSettings).done(ArchiveQueueService.onFetchTrxDetailsSuccess).fail(ArchiveQueueService.onFetchTrxDetailsFailure);
})


/*
$('.idBtn').click(function(event)
{
	const transactionId = event.target.id;
	if(!transactionId){
		console.error('idBtn.click: Error: Invalid transactionId!');
		return;
	}

	const requestSettings = {
    	url: '/plm/plmProp/' + transactionId,
    	method: 'GET',
    	accepts: {
    		mycustomtype: 'application/json'
    	},
    	statusCode:{
    	  400: function(){ArchiveQueueService.showErrorModal('Could not retrive plm transaction.');},
    	  500: function(){ArchiveQueueService.showErrorModal('Something went wrong processing the request.');}
    	}
    };

    $.ajax(requestSettings).done(ArchiveQueueService.onFetchTrxDetailsSuccess).fail(ArchiveQueueService.onFetchTrxDetailsFailure);
    ArchiveQueueService.showConfirmationModal('test');
});
*/

$(document).ready(function() {

});