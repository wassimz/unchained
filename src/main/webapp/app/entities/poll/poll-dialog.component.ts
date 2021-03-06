import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { Observable } from 'rxjs/Rx';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService } from 'ng-jhipster';

import { Poll } from './poll.model';
import { PollPopupService } from './poll-popup.service';
import { PollService } from './poll.service';

@Component({
    selector: 'jhi-poll-dialog',
    templateUrl: './poll-dialog.component.html'
})
export class PollDialogComponent implements OnInit {

    poll: Poll;
    authorities: any[];
    isSaving: boolean;
    expirationDp: any;
    options: any[];
    voters: any[];

    constructor(
        public activeModal: NgbActiveModal,
        private alertService: AlertService,
        private pollService: PollService,
        private eventManager: EventManager
    ) {

    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_ADMIN'];
        this.options = [{'id': 'option1', 'text': ''}];
        this.voters = [{'id': 'voter1', 'text': ''}];
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        this.setPollOptions();
        this.setPollVoters();

        if (this.poll.id !== undefined) {
            this.subscribeToSaveResponse(
                this.pollService.update(this.poll), false);
        } else {
            this.subscribeToSaveResponse(
                this.pollService.create(this.poll), true);
        }
    }

    addNewOption() {
        this.options.push({'id': 'option' + (this.options.length + 1), 'text': ''});
    }

    setPollOptions() {
        // Writes just the 'names' from the options object array into a csv string
        this.poll.options = Array.prototype.map.call(this.options, (s) => s.text).toString();
    }

    removeLastOption() {
        this.options.pop();
    }

    addNewVoter() {
        this.voters.push({'id': 'voter' + (this.voters.length + 1), 'text': ''});
    }

    setPollVoters() {
        // Writes just the 'names' from the options object array into a csv string
        this.poll.voters = Array.prototype.map.call(this.voters, (s) => s.text).toString();
    }

    removeLastVoter() {
        this.voters.pop();
    }

    private subscribeToSaveResponse(result: Observable<Poll>, isCreated: boolean) {
        result.subscribe((res: Poll) =>
            this.onSaveSuccess(res, isCreated), (res: Response) => this.onSaveError(res));
    }

    private onSaveSuccess(result: Poll, isCreated: boolean) {
        this.alertService.success(
            isCreated ? 'unchainedApp.poll.created'
            : 'unchainedApp.poll.updated',
            { param : result.id }, null);

        this.eventManager.broadcast({ name: 'pollListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}

@Component({
    selector: 'jhi-poll-popup',
    template: ''
})
export class PollPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private pollPopupService: PollPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['id'] ) {
                this.modalRef = this.pollPopupService
                    .open(PollDialogComponent, params['id']);
            } else {
                this.modalRef = this.pollPopupService
                    .open(PollDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
