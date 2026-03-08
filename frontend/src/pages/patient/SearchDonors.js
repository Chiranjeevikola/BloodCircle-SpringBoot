import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import API from '../../api';
import { FaSearch, FaFilter } from 'react-icons/fa';
import { BLOOD_GROUPS } from '../../constants';

export default function SearchDonors() {
  const [filters, setFilters] = useState({ blood_group: '', city: '', state: '' });
  const [results, setResults] = useState(null);
  const [page, setPage] = useState(1);
  const [pagination, setPagination] = useState(null);
  const [loading, setLoading] = useState(false);

  const set = (k) => (e) => setFilters({ ...filters, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  const search = async (p = 1) => {
    setLoading(true);
    try {
      const params = { page: p, per_page: 10 };
      if (filters.blood_group) params.blood_group = filters.blood_group;
      if (filters.city) params.city = filters.city;
      if (filters.state) params.state = filters.state;
      const r = await API.get('/patient/search', { params });
      setResults(r.data.donors);
      setPagination(r.data.pagination);
      setPage(p);
    } catch (err) {
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => { e.preventDefault(); search(1); };

  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title text-center"><FaSearch /> Search Donors</h1>

        <div className="card" style={{ marginBottom: '2rem' }}>
          <h3><FaFilter /> Filters</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>Blood Group</label>
                <select className="form-control" value={filters.blood_group} onChange={set('blood_group')}>
                  <option value="">All Groups</option>
                  {BLOOD_GROUPS.filter(Boolean).map(bg => <option key={bg} value={bg}>{bg}</option>)}
                </select>
              </div>
              <div className="form-group"><label>City</label><input className="form-control" value={filters.city} onChange={set('city')} placeholder="Any city" /></div>
              <div className="form-group"><label>State</label><input className="form-control" value={filters.state} onChange={set('state')} placeholder="Any state" /></div>
            </div>
            <div className="form-group">
              <label><input type="checkbox" checked={filters.compatible} onChange={set('compatible')} /> Show only compatible blood groups</label>
            </div>
            <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Searching...' : 'Search'}</button>
          </form>
        </div>

        {results !== null && (
          <div className="card">
            <h3>Results ({pagination?.total || 0} donors found)</h3>
            {results.length > 0 ? (
              <>
                <div className="table-responsive">
                  <table className="table">
                    <thead><tr><th>Name</th><th>Blood Group</th><th>City</th><th>State</th><th>Available</th><th>Action</th></tr></thead>
                    <tbody>
                      {results.map(d => (
                        <tr key={d.id}>
                          <td>{d.full_name}</td>
                          <td><span className="badge badge-danger">{d.blood_group}</span></td>
                          <td>{d.city}</td>
                          <td>{d.state}</td>
                          <td>{d.is_available ? <span className="badge badge-success">Yes</span> : <span className="badge badge-secondary">No</span>}</td>
                          <td><Link to={`/patient/donor/${d.id}`} className="btn btn-sm btn-outline">View</Link></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                {pagination && pagination.pages > 1 && (
                  <div className="pagination">
                    {page > 1 && <button className="btn btn-sm btn-outline" onClick={() => search(page - 1)}>Previous</button>}
                    <span style={{ padding: '0.5rem' }}>Page {page} of {pagination.pages}</span>
                    {page < pagination.pages && <button className="btn btn-sm btn-outline" onClick={() => search(page + 1)}>Next</button>}
                  </div>
                )}
              </>
            ) : <p>No donors found. Try adjusting your filters.</p>}
          </div>
        )}
      </div>
    </div>
  );
}
