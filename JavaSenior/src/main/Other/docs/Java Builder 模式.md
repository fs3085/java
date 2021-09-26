```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.apache.flink.connector.jdbc;

import java.io.Serializable;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.util.Preconditions;

@PublicEvolving
public class JdbcConnectionOptions implements Serializable {
    private static final long serialVersionUID = 1L;
    protected final String url;
    protected final String driverName;
    @Nullable
    protected final String username;
    @Nullable
    protected final String password;

    protected JdbcConnectionOptions(String url, String driverName, String username, String password) {
        this.url = (String)Preconditions.checkNotNull(url, "jdbc url is empty");
        this.driverName = (String)Preconditions.checkNotNull(driverName, "driver name is empty");
        this.username = username;
        this.password = password;
    }

    public String getDbURL() {
        return this.url;
    }

    public String getDriverName() {
        return this.driverName;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(this.username);
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(this.password);
    }

    public static class JdbcConnectionOptionsBuilder {
        private String url;
        private String driverName;
        private String username;
        private String password;

        public JdbcConnectionOptionsBuilder() {
        }

        public JdbcConnectionOptions.JdbcConnectionOptionsBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public JdbcConnectionOptions.JdbcConnectionOptionsBuilder withDriverName(String driverName) {
            this.driverName = driverName;
            return this;
        }

        public JdbcConnectionOptions.JdbcConnectionOptionsBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public JdbcConnectionOptions.JdbcConnectionOptionsBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public JdbcConnectionOptions build() {
            return new JdbcConnectionOptions(this.url, this.driverName, this.username, this.password);
        }
    }
}
```

```java
package connectors.mysql;

import org.apache.flink.api.common.io.DefaultInputSplitAssigner;
import org.apache.flink.api.common.io.InputFormat;
import org.apache.flink.api.common.io.RichInputFormat;
import org.apache.flink.api.common.io.statistics.BaseStatistics;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.internal.connection.SimpleJdbcConnectionProvider;
import org.apache.flink.connector.jdbc.internal.converter.JdbcRowConverter;
import org.apache.flink.connector.jdbc.split.JdbcParameterValuesProvider;
import org.apache.flink.connector.jdbc.table.JdbcRowDataInputFormat;
import org.apache.flink.core.io.GenericInputSplit;
import org.apache.flink.core.io.InputSplit;
import org.apache.flink.core.io.InputSplitAssigner;
import org.apache.flink.table.data.RowData;
import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class MysqlRowDataInputFormat extends RichInputFormat<RowData, InputSplit>
        implements ResultTypeQueryable<RowData> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(JdbcRowDataInputFormat.class);

    private JdbcConnectionOptions connectionOptions;
    private int fetchSize;
    private Boolean autoCommit;
    private Object[][] parameterValues;
    private String queryTemplate;
    private int resultSetType;
    private int resultSetConcurrency;
    private JdbcRowConverter rowConverter;
    private TypeInformation<RowData> rowDataTypeInfo;

    private transient Connection dbConn;
    private transient PreparedStatement statement;
    private transient ResultSet resultSet;
    private transient boolean hasNext;

    private MysqlRowDataInputFormat(
            JdbcConnectionOptions connectionOptions,
            int fetchSize,
            Boolean autoCommit,
            Object[][] parameterValues,
            String queryTemplate,
            int resultSetType,
            int resultSetConcurrency,
            JdbcRowConverter rowConverter,
            TypeInformation<RowData> rowDataTypeInfo) {
        this.connectionOptions = connectionOptions;
        this.fetchSize = fetchSize;
        this.autoCommit = autoCommit;
        this.parameterValues = parameterValues;
        this.queryTemplate = queryTemplate;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.rowConverter = rowConverter;
        this.rowDataTypeInfo = rowDataTypeInfo;
    }

    /**
     * A builder used to set parameters to the output format's configuration in a fluent way.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link JdbcRowDataInputFormat}. */
    public static class Builder {
        private JdbcConnectionOptions.JdbcConnectionOptionsBuilder connOptionsBuilder;
        private int fetchSize;
        private Boolean autoCommit;
        private Object[][] parameterValues;
        private String queryTemplate;
        private JdbcRowConverter rowConverter;
        private TypeInformation<RowData> rowDataTypeInfo;
        private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        private int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

        public Builder() {
            this.connOptionsBuilder = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder();
        }

        public Builder setDrivername(String drivername) {
            this.connOptionsBuilder.withDriverName(drivername);
            return this;
        }

        public Builder setDBUrl(String dbURL) {
            this.connOptionsBuilder.withUrl(dbURL);
            return this;
        }

        public Builder setUsername(String username) {
            this.connOptionsBuilder.withUsername(username);
            return this;
        }

        public Builder setPassword(String password) {
            this.connOptionsBuilder.withPassword(password);
            return this;
        }

        public Builder setQuery(String query) {
            this.queryTemplate = query;
            return this;
        }

        public Builder setParametersProvider(JdbcParameterValuesProvider parameterValuesProvider) {
            this.parameterValues = parameterValuesProvider.getParameterValues();
            return this;
        }

        public Builder setRowDataTypeInfo(TypeInformation<RowData> rowDataTypeInfo) {
            this.rowDataTypeInfo = rowDataTypeInfo;
            return this;
        }

        public Builder setRowConverter(JdbcRowConverter rowConverter) {
            this.rowConverter = rowConverter;
            return this;
        }

        public Builder setFetchSize(int fetchSize) {
            Preconditions.checkArgument(
                    fetchSize == Integer.MIN_VALUE || fetchSize > 0,
                    "Illegal value %s for fetchSize, has to be positive or Integer.MIN_VALUE.",
                    fetchSize);
            this.fetchSize = fetchSize;
            return this;
        }

        public Builder setAutoCommit(boolean autoCommit) {
            this.autoCommit = autoCommit;
            return this;
        }

        public Builder setResultSetType(int resultSetType) {
            this.resultSetType = resultSetType;
            return this;
        }

        public Builder setResultSetConcurrency(int resultSetConcurrency) {
            this.resultSetConcurrency = resultSetConcurrency;
            return this;
        }

        public MysqlRowDataInputFormat build() {
            if (this.queryTemplate == null) {
                throw new IllegalArgumentException("No query supplied");
            }
            if (this.rowConverter == null) {
                throw new IllegalArgumentException("No row converter supplied");
            }
            if (this.parameterValues == null) {
                LOG.debug("No input splitting configured (data will be read with parallelism 1).");
            }
            return new MysqlRowDataInputFormat(
                    connOptionsBuilder.build(),
                    this.fetchSize,
                    this.autoCommit,
                    this.parameterValues,
                    this.queryTemplate,
                    this.resultSetType,
                    this.resultSetConcurrency,
                    this.rowConverter,
                    this.rowDataTypeInfo);
        }
    }
}
```

```java
MysqlRowDataInputFormat.Builder builder1 = MysqlRowDataInputFormat.builder();
MysqlRowDataInputFormat builder2 = new MysqlRowDataInputFormat.Builder().build();
final MysqlRowDataInputFormat.Builder builder =
      MysqlRowDataInputFormat.builder()
               .setDrivername(options.getDriverName())
               .setDBUrl(options.getDbURL())
               .setUsername(options.getUsername().orElse(null))
               .setPassword(options.getPassword().orElse(null))
               .setAutoCommit(readOptions.getAutoCommit());
```

https://juejin.cn/post/6844903746124644365