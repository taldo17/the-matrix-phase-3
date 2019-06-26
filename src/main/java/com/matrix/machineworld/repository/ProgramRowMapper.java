package com.matrix.machineworld.repository;

import com.matrix.machineworld.datamodel.Program;
import com.matrix.machineworld.datamodel.ProgramCategory;
import com.matrix.machineworld.notrelevant.DataInitializer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProgramRowMapper implements RowMapper<Program> {
    @Override
    public Program mapRow(ResultSet resultSet, int i) throws SQLException {
        Program program = new Program();
        program.setId(resultSet.getInt("id"));
        program.setName(resultSet.getString("name"));
        program.setMarkedForDeletionCounter(resultSet.getInt("markedForDeletionCounter"));
        program.setActive(resultSet.getBoolean("active"));
        program.setCategory(ProgramCategory.getByValue(resultSet.getInt("category")));
        program.setCpuUsage(resultSet.getDouble("cpuUsage"));
        program.setCreationDate(DataInitializer.DateUtils.asLocalDate(resultSet.getDate("cpuUsage")));
        program.setCreator(resultSet.getString("creator"));
        program.setLastActive(DataInitializer.DateUtils.asLocalDate(resultSet.getDate("lastActive")));
        program.setMemoryConsumption(resultSet.getDouble("memoryConsumption"));
        program.setPurpose(resultSet.getString("purpose"));
        return program;
    }
}
