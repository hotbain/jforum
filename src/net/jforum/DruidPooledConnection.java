package net.jforum;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import com.alibaba.druid.pool.DruidDataSource;

public class DruidPooledConnection extends DBConnection {

	private DruidDataSource ds;
	@Override
	public void init() throws Exception {
		ds=new DruidDataSource();
		ds.setUsername(SystemGlobals.getValue(ConfigKeys.DATABASE_CONNECTION_USERNAME));
		ds.setPassword(SystemGlobals.getValue(ConfigKeys.DATABASE_CONNECTION_PASSWORD));
		ds.setUrl(SystemGlobals.getValue(ConfigKeys.DATABASE_CONNECTION_STRING));
		ds.setDriverClassName(SystemGlobals.getValue(ConfigKeys.DATABASE_CONNECTION_DRIVER));
		this.extrafParams();
	}

	private void extrafParams()
	{
		String extra = SystemGlobals.getValue(ConfigKeys.DRUID_EXTRA_PARAMS);
		
		if (extra != null && extra.trim().length() > 0) {
			String[] p = extra.split(";");
			
			for (int i = 0; i < p.length; i++) {
				String[] kv = p[i].trim().split("=");
				
				if (kv.length == 2) {
					this.invokeSetter(kv[0], kv[1]);
				}
			}
		}
	}
	

	/**
	 * Huge hack to invoke methods without the need of an external configuration file
	 * and whithout knowing the argument's type
	 */
	private void invokeSetter(String propertyName, String value)
	{
		try {
			String setter = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
			
			Method[] methods = this.ds.getClass().getMethods();
			
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				
				if (method.getName().equals(setter)) {
					Class[] paramTypes = method.getParameterTypes();
					
					if (paramTypes[0] == String.class) {
						method.invoke(this.ds, new Object[] { value });
					}
					else if (paramTypes[0] == int.class) {
						method.invoke(this.ds, new Object[] { new Integer(value) });
					}
					else if (paramTypes[0] == boolean.class) {
						method.invoke(this.ds, new Object[] { Boolean.valueOf(value) });
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void releaseConnection(Connection conn) {
		if (conn==null) {
            return;
        }

        try {
			conn.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void realReleaseAllConnections() throws Exception {
		ds.close();
	}

}
