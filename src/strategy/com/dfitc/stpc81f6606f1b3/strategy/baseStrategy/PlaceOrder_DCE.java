package com.dfitc.stpc81f6606f1b3.strategy.baseStrategy;

import com.dfitc.stp.annotations.*;
import com.dfitc.stp.indicator.*;
import com.dfitc.stp.market.*;
import com.dfitc.stp.trader.*;
import com.dfitc.stp.strategy.*;
import com.dfitc.stp.entity.Position;
import com.dfitc.stp.entity.Time;
import com.dfitc.stp.util.MathUtil;
import com.dfitc.stp.util.StringUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



/**
  	 *策略描述:
  	 * 策略开发目的：大商所品种下单测试专用，应用服务器221
   	 */
	
	
@Strategy(name = "下单策略__大商所用",version="1.0",outputMode = OutputMode.TIMER, outputPeriod = 3000, contractNumber = 1)
public class PlaceOrder_DCE extends BaseStrategy {
	

	@In(label = "请选择操作类型", sequence = 0)
	@Combo(readonly = true, selectIndex = 0, items = {
				@Item(key = "报单", value = "10"),@Item(key = "撤单", value = "11")
				 })
	int action;
	
	

	@In(label = "买卖方向", sequence = 1)
	@Direction(1)
	int direct;
	
	

	@In(label = "开平方向", sequence = 2)
	@Offset(1)
	int openCloseFlag;
	
	

	@In(label = "投保类型", sequence = 3)
	@Combo(readonly = true, selectIndex = 0, items = {
				@Item(key = "投机", value = "1"),@Item(key = "套利", value = "2"),@Item(key = "套保", value = "3")
				 })
	String insure_Type;
	
	

	@In(label = "定单类型", sequence = 4)
	@Combo(readonly = true, selectIndex = 0, items = {
				@Item(key = "限价委托", value = "1"),@Item(key = "市价委托", value = "2"),@Item(key = "套利委托", value = "4"),@Item(key = "展期互换", value = "8")
				 })
	String order_Type;
	
	

	@In(label = "定单属性", sequence = 5)
	@Combo(readonly = true, selectIndex = 0, items = {
				@Item(key = "缺省", value = "0"),@Item(key = "FOK", value = "1"),@Item(key = "FAK", value = "2")
				 })
	String order_Attribute;
	
	

	@In(label = "止损价和止盈价", sequence = 6)
	@Text(value = "0.0", readonly = false)
	double stop_Price;
	
	

	@In(label = "申报标志", sequence = 7)
	@Combo(readonly = true, selectIndex = 0, items = {
				@Item(key = "非自动单", value = "1"),@Item(key = "自动单", value = "2")
				 })
	String declare_Flag;
	
	

	@In(label = "下单价格", sequence = 8)
	@Text(value = "0.0", readonly = false)
	double price;
	
	

	@In(label = "下单数量", sequence = 9)
	@Text(value = "1", readonly = false)
	int vol;
	
	

	@In(label = "撤单委托号", sequence = 10)
	@Text(value = "000000000000000000", readonly = false)
	String OrderNumber;
	
	

//	@Out(label = "未成交报单委托号", sequence = 11)
//	
//	List<String> unopenedorderid=new ArrayList<String>();
	
	OrderRelevant order;
	double tick;
	int x;
	
 	/**
	 
 * 合约号：contracts[0] 周期：TICK 方法描述：
	 */
	@Override
	public void setBarCycles(String[] contracts) {
	}

	/**
	 
 * 初始化指标，在策略创建时被调用(在initialize之后调用)	
 * @param contracts策略相关联的合约
	 */
	@Override
	public void setIndicators(String[] contracts) {
		
	}
	
	/**
	 * 初始化方法，在策略创建时调用
	 * 
	 * @param contracts策略关联的合约
	 */
	@Override
	public void initialize(String[] contracts) {
		tick = this.getMinMove();
		loadPosition(getBindAccountID(0));
	}
	
	@Override
	public boolean acceptPosition(Position pos) {
		
		return true;
	}
	
	@Override
	public void postStartStrategy() {
		x=0;
		if (action == 10) { // 报单
			// order = this.placeOrder(price, vol, direct,OpenCloseFlag );
			order = orderInsert(getContractCode(),
					MathUtil.lower(price + tick / 2, tick), vol, direct,
					openCloseFlag, insure_Type, order_Type, order_Attribute,
					stop_Price, declare_Flag);
			// order = this.placeOrder(MathUtil.lower(price + tick / 2, tick),
			// vol, direct, openCloseFlag);
//			if (order.getRemainderVol() != 0) {
//				unopenedorderid.add(order.getLocalID());
//			}
		} else if (action == 11) { // 撤单
			this.orderCancelSync(this.getOrderRelevant(OrderNumber));
		}
	}
	@Override
	public void postResumeStrategy() {
		x=0;
		if (action == 10) { // 报单
			// order = this.placeOrder(price, vol, direct,OpenCloseFlag );
			order = orderInsert(getContractCode(),
					MathUtil.lower(price + tick / 2, tick), vol, direct,
					openCloseFlag, insure_Type, order_Type, order_Attribute,
					stop_Price, declare_Flag);
			// order = this.placeOrder(MathUtil.lower(price + tick / 2, tick),
			// vol, direct, openCloseFlag);
//			if (order.getRemainderVol() != 0) {
//				unopenedorderid.add(order.getLocalID());
//			}
		} else if (action == 11) { // 撤单
			this.orderCancelSync(this.getOrderRelevant(OrderNumber));
		}
	}
	/**
	 * 处理K线
	 * 
	 * @param k触发此次调用的K线
	 * @param kseries此次K线所对应的K线序列
	 *            (kseries.get()与k是等价的)
	 */
	public void processBar(Bar bar, BarSeries barSeries) {
	}
	/**
	 * 处理TICK行情
	 * 
	 * @param market触发此次调用的行情快照
	 */
	@Override
	public void processMD(MD md) {
		x++;
		if(x==1){
			System.out.println("涨停："+md.getUpperLimitPrice());
			System.out.println("跌停："+md.getLowerLimitPrice());
		}
		
	}
	/**
	 * 处理委托回报
	 * 
	 * @param answer触发此次调用的回报
	 * @param order此次回报对应的报单
	 * @param market此回报对应合约的最新Tick行情快照
	 */
	@Override
	public void processOrderStatus(OrderStatusResult result,
			OrderRelevant order, MD md) {
		//System.out.println(order);
	}
	/**
	 * 处理成交回报
	 * 
	 * @param answer触发此次调用的回报
	 * @param order此次回报对应的报单
	 * @param market此回报对应合约的最新Tick行情快照
	 */
	@Override
	public void processOrderDeal(OrderDealResult result, OrderRelevant order,
			MD mdt) {
//		if (order.getRemainderVol() == 0) {
//			unopenedorderid.remove(order.getLocalID());
//			
//		}
	}
	/**
	 * 处理撤单回报
	 * 
	 * @param answer触发此次调用的回报
	 * @param order此次回报对应的报单
	 * @param market此回报对应合约的最新Tick行情快照
	 */
	@Override
	public void processOrderCancel(OrderCancelResult result,
			OrderRelevant order, MD md) {
		//unopenedorderid.remove(order.getLocalID());
	}
}
