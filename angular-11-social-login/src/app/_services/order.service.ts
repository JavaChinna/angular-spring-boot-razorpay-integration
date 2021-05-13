import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppConstants } from '../common/app.constants';

const httpOptions = {
headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
declare var Razorpay: any;

@Injectable({
providedIn: 'root'
})
export class OrderService {

	constructor(private http: HttpClient) {

	}
  
	createOrder(order): Observable<any> {
		return this.http.post(AppConstants.API_URL + 'order', {
		customerName: order.name,
		email: order.email,
		phoneNumber: order.phone,
		amount: order.amount
		}, httpOptions);
	}
  
	updateOrder(order): Observable<any> {
		return this.http.put(AppConstants.API_URL + 'order', {
		razorpayOrderId: order.razorpay_order_id,
		razorpayPaymentId: order.razorpay_payment_id,
		razorpaySignature: order.razorpay_signature
		}, httpOptions);
	}
}
