import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { DateUtils } from 'ng-jhipster';

import { Poll } from './poll.model';
import { ResponseWrapper, createRequestOption } from '../../shared';

@Injectable()
export class PollService {

    private resourceUrl = 'api/polls';

    constructor(private http: Http, private dateUtils: DateUtils) { }

    create(poll: Poll): Observable<Poll> {
        const copy = this.convert(poll);
        return this.http.post(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            this.convertItemFromServer(jsonResponse);
            return jsonResponse;
        });
    }

    update(poll: Poll): Observable<Poll> {
        const copy = this.convert(poll);
        return this.http.put(this.resourceUrl, copy).map((res: Response) => {
            const jsonResponse = res.json();
            this.convertItemFromServer(jsonResponse);
            return jsonResponse;
        });
    }

    find(id: number): Observable<Poll> {
        return this.http.get(`${this.resourceUrl}/${id}`).map((res: Response) => {
            const jsonResponse = res.json();
            this.convertItemFromServer(jsonResponse);
            return jsonResponse;
        });
    }

    query(req?: any): Observable<ResponseWrapper> {
        const options = createRequestOption(req);
        return this.http.get(this.resourceUrl, options)
            .map((res: Response) => this.convertResponse(res));
    }

    delete(id: number): Observable<Response> {
        return this.http.delete(`${this.resourceUrl}/${id}`);
    }

    private convertResponse(res: Response): ResponseWrapper {
        const jsonResponse = res.json();
        for (let i = 0; i < jsonResponse.length; i++) {
            this.convertItemFromServer(jsonResponse[i]);
        }
        return new ResponseWrapper(res.headers, jsonResponse, res.status);
    }

    private convertItemFromServer(entity: any) {
        entity.expiration = this.dateUtils
            .convertLocalDateFromServer(entity.expiration);
    }

    private convert(poll: Poll): Poll {
        const copy: Poll = Object.assign({}, poll);
        copy.expiration = this.dateUtils
            .convertLocalDateToServer(poll.expiration);
        return copy;
    }
}
