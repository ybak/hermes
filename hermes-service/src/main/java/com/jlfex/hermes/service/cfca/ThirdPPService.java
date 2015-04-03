package com.jlfex.hermes.service.cfca;

import cfca.payment.api.tx.Tx1361Response;
import cfca.payment.api.tx.Tx1362Request;
import cfca.payment.api.tx.Tx1362Response;

/**
 * 中金支付接口
 * 
 * @author wujinsong
 *
 */
public interface ThirdPPService {
	/**
	 * 单笔代收
	 * 
	 * @param request
	 * @return
	 */
	public Tx1361Response invokeTx1361(Tx1362Request request);

	/**
	 * 单笔代收查询
	 * 
	 * @param request
	 * @return
	 */
	public Tx1362Response invokeTx1362(Tx1362Request request);
}
