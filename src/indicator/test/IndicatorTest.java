package test;

import java.io.File;
import com.dfitc.studio.IndicatorRunner;

/**
 * 指标测试类
 */
public class IndicatorTest {

	/**
	 * 指标测试
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			return;
		}
		// 获取数据文件路径，并运行指标
		new IndicatorRunner().start(args, new File(IndicatorTest.class.getResource("/").toURI()));
	}

}
